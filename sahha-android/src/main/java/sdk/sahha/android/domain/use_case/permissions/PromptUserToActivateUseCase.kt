package sdk.sahha.android.domain.use_case.permissions

import android.content.Context
import sdk.sahha.android.source.SahhaActivityStatus
import sdk.sahha.android.domain.repository.PermissionsRepo
import javax.inject.Inject

class PromptUserToActivateUseCase @Inject constructor(
    private val repository: PermissionsRepo
) {
    operator fun invoke(context: Context, callback: (sahhaActivityStatus: Enum<SahhaActivityStatus>) -> Unit) {
        repository.promptUserToActivateActivityRecognition(context, callback)
    }
}