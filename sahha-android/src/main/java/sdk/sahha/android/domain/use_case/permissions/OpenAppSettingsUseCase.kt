package sdk.sahha.android.domain.use_case.permissions

import android.content.Context
import sdk.sahha.android.domain.manager.PermissionManager

class OpenAppSettingsUseCase (
    private val repository: PermissionManager
) {
    operator fun invoke(context: Context) {
        repository.openAppSettings(context)
    }
}