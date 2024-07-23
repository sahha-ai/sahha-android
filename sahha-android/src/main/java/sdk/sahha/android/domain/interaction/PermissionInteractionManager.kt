package sdk.sahha.android.domain.interaction

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.Session
import sdk.sahha.android.di.DefaultScope
import sdk.sahha.android.di.MainScope
import sdk.sahha.android.domain.internal_enum.InternalSensorStatus
import sdk.sahha.android.domain.internal_enum.toSahhaSensorStatus
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.model.callbacks.ActivityCallback
import sdk.sahha.android.domain.model.config.toSahhaSensorSet
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.use_case.permissions.OpenAppSettingsUseCase
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "PermissionInteractionManager"

internal class PermissionInteractionManager @Inject constructor(
    internal val manager: PermissionManager,
    private val openAppSettingsUseCase: OpenAppSettingsUseCase,
    private val activityCallback: ActivityCallback,
    private val configRepo: SahhaConfigRepo,
    private val sensorRepo: SensorRepo,
    @DefaultScope private val defaultScope: CoroutineScope,
    @MainScope private val mainScope: CoroutineScope,
) {
    fun openAppSettings(context: Context) {
        openAppSettingsUseCase(context)
    }

    fun enableSensors(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        defaultScope.launch {
            val sensors = configRepo.getConfig().sensorArray.toSahhaSensorSet()
            val sensorSetEmpty = sensors.isEmpty()
            if (sensorSetEmpty) {
                callback(SahhaErrors.dataTypesUnspecified, SahhaSensorStatus.pending)
                return@launch
            }

            if (Session.onlyDeviceSensorProvided) {
                manager.enableDeviceOnlySensor { status ->
                    if (status == SahhaSensorStatus.enabled) {
                        startNativeTasks(
                            context,
                            Sahha.di.sahhaInteractionManager,
                            status
                        )
                        callback(null, status)
                    }
                }
                return@launch
            }

            val containsStepsOrSleep =
                sensors.contains(SahhaSensor.step_count) || sensors.contains(SahhaSensor.sleep)
            val nativeStatus =
                if (containsStepsOrSleep) awaitNativeSensorRequest(context) else SahhaSensorStatus.enabled
            val healthConnectStatus = awaitHealthConnectSensorRequest(context, nativeStatus)
            val appUsageStatus = awaitAppUsageRequest(context = context)

            val processedStatus = processStatuses(nativeStatus, healthConnectStatus.second)
            startTasks(
                context = context,
                sim = Sahha.di.sahhaInteractionManager,
                status = processedStatus,
                previousError = healthConnectStatus.first,
                callback = callback
            )
            // TODO: Additionally process and start tasks for app usage
        }
    }

    private suspend fun awaitAppUsageRequest(
        context: Context
    ) = suspendCancellableCoroutine { cont ->
        val shouldShowRationale = manager.appUsageDenialCount < 2

        if (shouldShowRationale) manager.appUsageSettings(context) { error, status ->
            logError(error)
            if (cont.isActive) cont.resume(status)
        }
    }

    private fun logError(error: String?) {
        error?.also { e -> Log.d(TAG, e) }
    }

    private fun startTasks(
        context: Context,
        sim: SahhaInteractionManager,
        status: Enum<InternalSensorStatus>,
        previousError: String? = null,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)? = null
    ) {
        when (manager.shouldUseHealthConnect()) {
            true -> {
                when (status) {
                    InternalSensorStatus.partial -> startNativeAndHealthConnectTasks(
                        context,
                        sim,
                        status.toSahhaSensorStatus(),
                        callback
                    )

                    InternalSensorStatus.enabled -> startNativeAndHealthConnectTasks(
                        context,
                        sim,
                        status.toSahhaSensorStatus(),
                        callback
                    )

                    InternalSensorStatus.disabled -> callback?.invoke(
                        previousError,
                        SahhaSensorStatus.disabled
                    )

                    InternalSensorStatus.unavailable -> callback?.invoke(
                        previousError,
                        SahhaSensorStatus.unavailable
                    )

                    else -> callback?.invoke(previousError, SahhaSensorStatus.pending)
                }
            }

            false -> {
                when (status) {
                    InternalSensorStatus.partial -> startNativeTasks(
                        context = context,
                        sim = sim,
                        status = status.toSahhaSensorStatus(),
                        callback = callback
                    )

                    InternalSensorStatus.enabled -> startNativeTasks(
                        context = context,
                        sim = sim,
                        status = status.toSahhaSensorStatus(),
                        callback = callback
                    )

                    InternalSensorStatus.disabled -> callback?.invoke(
                        null,
                        SahhaSensorStatus.disabled
                    )

                    InternalSensorStatus.unavailable -> callback?.invoke(
                        null,
                        SahhaSensorStatus.unavailable
                    )

                    else -> callback?.invoke(null, SahhaSensorStatus.pending)
                }
            }
        }

    }

    private fun startNativeTasks(
        context: Context,
        sim: SahhaInteractionManager,
        status: Enum<SahhaSensorStatus>,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)? = null
    ) {
        sim.startNative(context) { error, success ->
            if (success) {
                callback?.invoke(null, status)
                return@startNative
            }

            error?.also { e -> callback?.invoke(e, status) }
        }
    }

    private fun startNativeAndHealthConnectTasks(
        context: Context,
        sim: SahhaInteractionManager,
        status: Enum<SahhaSensorStatus>,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)? = null
    ) {
        sim.startNativeAndHealthConnect(context) { error, success ->
            if (success) {
                callback?.invoke(null, status)
                return@startNativeAndHealthConnect
            }

            error?.also { e -> callback?.invoke(e, status) }
        }
    }

    private fun processStatuses(
        nativeStatus: Enum<SahhaSensorStatus>,
        healthConnectStatus: Enum<SahhaSensorStatus>
    ): Enum<InternalSensorStatus> {
        val nativeUnavailable = nativeStatus == SahhaSensorStatus.unavailable
        val nativePending = nativeStatus == SahhaSensorStatus.pending
        val nativeDisabled = nativeStatus == SahhaSensorStatus.disabled
        val healthConnectUnavailable = healthConnectStatus == SahhaSensorStatus.unavailable
        val healthConnectDisabled = healthConnectStatus == SahhaSensorStatus.disabled
                || healthConnectStatus == SahhaSensorStatus.unavailable
        val nativeEnabled = nativeStatus == SahhaSensorStatus.enabled
        val healthConnectEnabled = healthConnectStatus == SahhaSensorStatus.enabled

        val disabled = nativeDisabled && healthConnectDisabled
        val partialNative =
            nativeEnabled && healthConnectDisabled && manager.shouldUseHealthConnect()
        val requested = nativeEnabled && healthConnectEnabled
        val onlyNativeAvailableAndEnabled = nativeEnabled && !manager.shouldUseHealthConnect()

        return when {
            nativeUnavailable -> InternalSensorStatus.unavailable
            healthConnectUnavailable -> InternalSensorStatus.unavailable
            nativePending -> InternalSensorStatus.pending
            partialNative -> InternalSensorStatus.partial
            onlyNativeAvailableAndEnabled -> InternalSensorStatus.enabled
            requested -> InternalSensorStatus.enabled
            disabled -> InternalSensorStatus.disabled
            else -> InternalSensorStatus.pending
        }
    }

    private suspend fun awaitHealthConnectSensorRequest(
        context: Context,
        nativeStatus: Enum<SahhaSensorStatus>
    ): Pair<String?, Enum<SahhaSensorStatus>> {
        return suspendCancellableCoroutine { cont ->
            val nativeDisabled =
                nativeStatus == SahhaSensorStatus.disabled
                        || nativeStatus == SahhaSensorStatus.unavailable
                        || nativeStatus == SahhaSensorStatus.pending
            if (!manager.shouldUseHealthConnect()) {
                if (cont.isActive) cont.resume(Pair(null, SahhaSensorStatus.unavailable))
                return@suspendCancellableCoroutine
            }
            if (nativeDisabled) {
                if (cont.isActive) cont.resume(Pair(null, SahhaSensorStatus.disabled))
                return@suspendCancellableCoroutine
            }

            manager.requestHealthConnectSensors(context) { _, _ ->
                manager.isFirstHealthConnectRequest(false)
                manager.getHealthConnectSensorStatus(
                    context,
                    Session.sensors ?: setOf()
                ) { error, status ->
                    if (cont.isActive) cont.resume(Pair(error, status))
                }
            }
        }
    }

    private suspend fun awaitNativeSensorRequest(
        context: Context
    ): Enum<SahhaSensorStatus> {
        return suspendCoroutine { cont ->
            manager.requestNativeSensors(context) { status ->
                cont.resume(status)
            }
        }
    }

    private suspend fun awaitHealthConnectSensorStatus(context: Context): Enum<SahhaSensorStatus> {
        val storedSensors = configRepo.getConfig().sensorArray.toSahhaSensorSet()
        return suspendCancellableCoroutine { cont ->
            manager.getHealthConnectSensorStatus(
                context = context,
                sensors = Session.sensors ?: storedSensors
            ) { _, status ->
                if (cont.isActive) cont.resume(status)
            }
        }
    }

    private fun getHealthConnectSensorStatus(
        context: Context,
        sensors: Set<SahhaSensor>,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        manager.getHealthConnectSensorStatus(
            context = context,
            sensors = sensors,
            callback = callback
        )
    }

    private suspend fun awaitNativeSensorStatus(
        context: Context
    ): Enum<SahhaSensorStatus> {
        return suspendCoroutine { cont ->
            manager.getNativeSensorStatus(context) { status ->
                cont.resume(status)
            }
        }
    }

    fun getSensorStatus(
        context: Context,
        sensors: Set<SahhaSensor>,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        mainScope.launch {
            Session.sensors = sensors
            val sensorSetEmpty = sensors.isEmpty()
            if (sensorSetEmpty) {
                callback(SahhaErrors.dataTypesUnspecified, SahhaSensorStatus.pending)
                return@launch
            }

            if (Session.onlyDeviceSensorProvided) {
                manager.getDeviceOnlySensorStatus { status -> callback(null, status) }
                return@launch
            }

            val containsStepsOrSleep =
                sensors.contains(SahhaSensor.step_count) || sensors.contains(SahhaSensor.sleep)
            val nativeStatus =
                if (containsStepsOrSleep) awaitNativeSensorStatus(context) else SahhaSensorStatus.enabled

            if (manager.shouldUseHealthConnect())
                getHealthConnectSensorStatus(context, sensors) { error, status ->
                    val processedStatus = processStatuses(nativeStatus, status)
                    callback(error, processedStatus.toSahhaSensorStatus())
                }
            else {
                val status = processStatuses(nativeStatus, SahhaSensorStatus.unavailable)
                callback(null, status.toSahhaSensorStatus())
            }
        }
    }

    suspend fun startHcOrNativeDataCollection(
        context: Context,
        callback: ((error: String?, successful: Boolean) -> Unit)? = null
    ) {
        if (!Sahha.isAuthenticated) {
            callback?.invoke("Not yet authenticated", false)
            return
        }

        if (Session.onlyDeviceSensorProvided) {
            manager.getDeviceOnlySensorStatus { status ->
                if (status == SahhaSensorStatus.enabled) {
                    stopWorkers()
                    startNativeTasks(
                        context,
                        Sahha.di.sahhaInteractionManager,
                        status,
                    ) { _, _ -> callback?.invoke(null, true) }
                } else callback?.invoke(null, true)
            }
            return
        }

        val sensors = configRepo.getConfig().sensorArray.toSahhaSensorSet()
        val containsStepsOrSleep =
            sensors.contains(SahhaSensor.step_count) || sensors.contains(SahhaSensor.sleep)
        val nativeStatus =
            if (containsStepsOrSleep) awaitNativeSensorStatus(context) else SahhaSensorStatus.enabled
        val healthConnectStatus = awaitHealthConnectSensorStatus(context)
        val status = processStatuses(nativeStatus, healthConnectStatus)
        stopWorkers()
        startTasks(
            context,
            Sahha.di.sahhaInteractionManager,
            status // Already processed from internal status
        ) { error, _ ->
            error?.also { e -> callback?.invoke(e, false) } ?: callback?.invoke(null, true)
        }
    }

    private fun stopWorkers() {
        sensorRepo.stopAllWorkers()
    }
}