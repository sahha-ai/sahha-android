package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.PermissionsRepo
import javax.inject.Inject

class GrantActivityRecognitionPermissionUseCase @Inject constructor(
    private val repository: PermissionsRepo
) {
    operator fun invoke() {
        repository.grantActivityRecognition()
    }
}