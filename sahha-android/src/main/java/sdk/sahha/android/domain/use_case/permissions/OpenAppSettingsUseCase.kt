package sdk.sahha.android.domain.use_case.permissions

import android.content.Context
import sdk.sahha.android.domain.manager.PermissionManager
import javax.inject.Inject

class OpenAppSettingsUseCase @Inject constructor (
    private val repository: PermissionManager
) {
    operator fun invoke(context: Context) {
        repository.openAppSettings(context)
    }
}