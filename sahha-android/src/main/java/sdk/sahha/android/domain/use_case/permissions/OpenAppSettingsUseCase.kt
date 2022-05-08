package sdk.sahha.android.domain.use_case.permissions

import android.content.Context
import sdk.sahha.android.domain.repository.PermissionsRepo
import javax.inject.Inject

class OpenAppSettingsUseCase @Inject constructor(
    private val repository: PermissionsRepo
) {
    operator fun invoke(context: Context) {
        repository.openAppSettings(context)
    }
}