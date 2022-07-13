package sdk.sahha.android.domain.use_case.permissions

import android.content.Context
import sdk.sahha.android.domain.repository.PermissionsRepo

class OpenAppSettingsUseCase (
    private val repository: PermissionsRepo
) {
    operator fun invoke(context: Context) {
        repository.openAppSettings(context)
    }
}