package sdk.sahha.android.domain.use_case.permissions

import android.content.Context
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject

class OpenAppSettingsUseCase @Inject constructor(
    private val manager: PermissionManager,
) {
    operator fun invoke(context: Context) {
        when (manager.shouldUseHealthConnect()) {
            true -> {
                manager.getNativeSensorStatus(context) { status ->
                    when (status) {
                        SahhaSensorStatus.enabled -> manager.openHealthConnectSettings(context)
                        else -> manager.openAppSettings(context)
                    }
                }
            }

            false -> {
                manager.openAppSettings(context)
            }
        }
    }
}