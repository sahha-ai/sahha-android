package sdk.sahha.android.domain.interaction

import android.content.Context
import kotlinx.coroutines.suspendCancellableCoroutine
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.model.callbacks.ActivityCallback
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.use_case.permissions.OpenAppSettingsUseCase
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject
import kotlin.coroutines.resume

private const val tag = "PermissionInteractionManager"

class PermissionInteractionManager @Inject constructor(
    internal val manager: PermissionManager,
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
        manager.enableSensors(context, callback)
    }

    fun getSensorStatus(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        manager.getSensorStatus(context, callback)
    }

    suspend fun checkHcAvailabilityAndStart(
        context: Context,
        callback: ((error: String?, successful: Boolean) -> Unit)? = null
    ) {
        val status = awaitStatus(context)
        stopWorkersAndSetConfig()
        when (status) {
            SahhaSensorStatus.enabled -> Sahha.sim.startHealthConnect(callback)
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