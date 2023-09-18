package sdk.sahha.android.interaction

import android.content.Context
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.use_case.permissions.OpenAppSettingsUseCase
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject

class PermissionInteractionManager @Inject constructor(
    private val permissionManager: PermissionManager,
    private val openAppSettingsUseCase: OpenAppSettingsUseCase
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

    fun checkPermissionsAndStart(
        context: Context,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        permissionManager.checkAndStart(context, callback)
    }
}