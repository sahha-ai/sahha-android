package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.PermissionsRepo
import javax.inject.Inject

class OpenSettingsUseCase @Inject constructor(
    private val repository: PermissionsRepo
) {
    operator fun invoke() {
        repository.openSettings()
    }
}