package sdk.sahha.android.domain.use_case.permissions

import sdk.sahha.android.domain.model.enums.ActivityStatus
import sdk.sahha.android.domain.repository.PermissionsRepo
import javax.inject.Inject

class PromptUserToActivateUseCase @Inject constructor(
    private val repository: PermissionsRepo
) {
    operator fun invoke(callback: (activityStatus: Enum<ActivityStatus>) -> Unit) {
        repository.promptUserToActivateActivityRecognition(callback)
    }
}