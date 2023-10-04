package sdk.sahha.android.interaction

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import sdk.sahha.android.activity.health_connect.SahhaHealthConnectStatusActivity
import sdk.sahha.android.common.SahhaReceiversAndListeners
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.model.callbacks.ActivityCallback
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.use_case.permissions.OpenAppSettingsUseCase
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val tag = "PermissionInteractionManager"

class PermissionInteractionManager @Inject constructor(
    private val permissionManager: PermissionManager,
    private val openAppSettingsUseCase: OpenAppSettingsUseCase,
    private val activityCallback: ActivityCallback,
    private val configRepo: SahhaConfigRepo,
    private val sensorRepo: SensorRepo
) {
    fun openAppSettings(context: Context) {
        openAppSettingsUseCase(context)
    }

    fun enableSensors(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        permissionManager.enableSensors(context, callback)
    }

    fun getSensorStatus(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        permissionManager.getSensorStatus(context, callback)
    }

    suspend fun checkPermissionsAndStart(
        context: Context,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        permissionManager.checkAndStart(context, callback)
    }

    suspend fun checkHcAvailabilityAndStart(
        context: Context
    ) {
        val status = awaitHcStatus(context)
        println("checkHcAvailabilityAndStart0001")
        when (status) {
            SahhaSensorStatus.enabled -> {
                println("checkHcAvailabilityAndStart0002")
                stopWorkersAndSetConfig()
                checkAndStartScreenStateCollection(context)
            }

            SahhaSensorStatus.unavailable -> {
                stopWorkersAndSetConfig()
                Sahha.sim.startNative()
            }
        }
        println("checkHcAvailabilityAndStart0003")
    }

    private suspend fun stopWorkersAndSetConfig() {
        sensorRepo.stopAllWorkers()
        Sahha.config = configRepo.getConfig()
    }

    private suspend fun checkAndStartScreenStateCollection(
        context: Context
    ) {
        val config = configRepo.getConfig()
        if (config.sensorArray.contains(SahhaSensor.device.ordinal)) {
            tryUnregisterExistingReceiver(context, SahhaReceiversAndListeners.screenLocks)
            Sahha.sim.sensor.startCollectingPhoneScreenLockDataUseCase(context)

            sensorRepo.startDevicePostWorker(
                Constants.WORKER_REPEAT_INTERVAL_MINUTES,
                Constants.DEVICE_POST_WORKER_TAG
            )
        }
    }

    private suspend fun awaitHcStatus(
        context: Context,
    ): Enum<SahhaSensorStatus> =
        suspendCoroutine { cont ->
            getHcStatus(context) { _, status ->
                cont.resume(status)
            }
        }

    private fun getHcStatus(
        context: Context,
        callback: (error: String?, status: Enum<SahhaSensorStatus>) -> Unit
    ) {
        activityCallback.statusCallback = callback
        val intent =
            Intent(context, SahhaHealthConnectStatusActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun tryUnregisterExistingReceiver(
        context: Context,
        receiver: BroadcastReceiver
    ) {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            Log.w(
                tag,
                e.message ?: "Could not unregister receiver or listener",
                e
            )
        }
    }
}