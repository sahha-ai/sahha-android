package sdk.sahha.android.domain.interaction

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import sdk.sahha.android.di.DefaultScope
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.model.callbacks.ActivityCallback
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.use_case.permissions.OpenAppSettingsUseCase
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val tag = "PermissionInteractionManager"

class PermissionInteractionManager @Inject constructor(
    internal val manager: PermissionManager,
    private val openAppSettingsUseCase: OpenAppSettingsUseCase,
    private val activityCallback: ActivityCallback,
    private val configRepo: SahhaConfigRepo,
    private val sensorRepo: SensorRepo,
    @DefaultScope private val defaultScope: CoroutineScope,
) {
    fun openAppSettings(context: Context) {
        openAppSettingsUseCase(context)
    }

    fun enableSensors(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        defaultScope.launch {
            val nativeStatus = awaitNativeSensorRequest(context)
            val healthConnectStatus = awaitHealthConnectSensorRequest(context, nativeStatus)

            val status = processStatuses(nativeStatus, healthConnectStatus)
            startTasks(
                context,
                Sahha.di.sahhaInteractionManager,
                status,
                callback
            )
        }
    }

    private fun startTasks(
        context: Context,
        sim: SahhaInteractionManager,
        status: Enum<SahhaSensorStatus>,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)? = null
    ) {
        when (status) {
            SahhaSensorStatus.partiallyRequested -> startNativeTasks(context, sim, status, callback)
            SahhaSensorStatus.requested -> startNativeAndHealthConnectTasks(
                context,
                sim,
                status,
                callback
            )

            SahhaSensorStatus.disabled -> callback?.invoke(null, SahhaSensorStatus.disabled)
            SahhaSensorStatus.unavailable -> callback?.invoke(null, SahhaSensorStatus.unavailable)
            else -> callback?.invoke(null, SahhaSensorStatus.pending)
        }
    }

    private fun startNativeTasks(
        context: Context,
        sim: SahhaInteractionManager,
        status: Enum<SahhaSensorStatus>,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)? = null
    ) {
        sim.startNative(context) { error, success ->
            if (success)
                manager.getNativeSensorStatus(context) { status ->
                    callback?.invoke(null, status)
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
        sim.startNative(context) { error, success ->
            if (success)
                sim.startHealthConnect(context) { hcError, hcSuccess ->
                    if (hcSuccess)
                        manager.getHealthConnectSensorStatus { status ->
                            callback?.invoke(null, status)
                        }

                    hcError?.also { e -> callback?.invoke(e, status) }
                }

            error?.also { e -> callback?.invoke(e, status) }
        }
    }

    private fun processStatuses(
        nativeStatus: Enum<SahhaSensorStatus>,
        healthConnectStatus: Enum<SahhaSensorStatus>
    ): Enum<SahhaSensorStatus> {
        val nativeDisabled =
            nativeStatus == SahhaSensorStatus.disabled
                    || nativeStatus == SahhaSensorStatus.unavailable
                    || nativeStatus == SahhaSensorStatus.pending
        val healthConnectDisabled = healthConnectStatus == SahhaSensorStatus.disabled
                || healthConnectStatus == SahhaSensorStatus.unavailable
                || healthConnectStatus == SahhaSensorStatus.pending
        val nativeEnabled = nativeStatus == SahhaSensorStatus.requested
        val healthConnectEnabled = healthConnectStatus == SahhaSensorStatus.requested

        val pending =
            nativeStatus == SahhaSensorStatus.pending || healthConnectStatus == SahhaSensorStatus.pending
        val disabled = nativeDisabled && healthConnectDisabled
        val partialNative = nativeEnabled && healthConnectDisabled
        val requested = nativeEnabled && healthConnectEnabled

        return when {
            pending -> SahhaSensorStatus.pending
            disabled -> SahhaSensorStatus.disabled
            partialNative -> SahhaSensorStatus.partiallyRequested
            requested -> SahhaSensorStatus.requested
            else -> SahhaSensorStatus.unavailable
        }
    }

    private suspend fun awaitHealthConnectSensorRequest(
        context: Context,
        nativeStatus: Enum<SahhaSensorStatus>
    ): Enum<SahhaSensorStatus> {
        return suspendCoroutine { cont ->
            val nativeDisabled =
                nativeStatus == SahhaSensorStatus.disabled
                        || nativeStatus == SahhaSensorStatus.unavailable
                        || nativeStatus == SahhaSensorStatus.pending
            if (!manager.shouldUseHealthConnect()) {
                cont.resume(SahhaSensorStatus.unavailable)
                return@suspendCoroutine
            }
            if (nativeDisabled) {
                cont.resume(SahhaSensorStatus.unavailable)
                return@suspendCoroutine
            }

            manager.requestHealthConnectSensors(context) { _, status ->
                cont.resume(status)
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
        return suspendCoroutine { cont ->
            manager.getHealthConnectSensorStatus { status ->
                cont.resume(status)
            }
        }
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
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        defaultScope.launch {
            val nativeStatus = awaitNativeSensorStatus(context)
            val healthConnectStatus =
                if (manager.shouldUseHealthConnect())
                    awaitHealthConnectSensorStatus(context)
                else SahhaSensorStatus.unavailable

            val status = processStatuses(nativeStatus, healthConnectStatus)
            startTasks(
                context,
                Sahha.di.sahhaInteractionManager,
                status,
                callback
            )
        }
    }

    suspend fun startHcOrNativeDataCollection(
        context: Context,
        callback: ((error: String?, successful: Boolean) -> Unit)? = null
    ) {
        val status = awaitStatus(context)
        stopWorkersAndSetConfig()
        when (status) {
            SahhaSensorStatus.requested -> {
                startTasks(context, Sahha.di.sahhaInteractionManager, status) { error, status ->
                    error?.also { e -> callback?.invoke(e, false) } ?: callback?.invoke(null, true)
                }
            }

            else -> callback?.invoke(null, true)
        }
    }

    private suspend fun stopWorkersAndSetConfig() {
        sensorRepo.stopAllWorkers()
        Sahha.config = configRepo.getConfig()
    }

    private suspend fun awaitStatus(context: Context): Enum<SahhaSensorStatus> =
        suspendCancellableCoroutine { cont ->
            getSensorStatus(context) { _, status ->
                if (cont.isActive)
                    cont.resume(status)
            }
        }
}