package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.data.remote.dto.send.StepSendDto
import sdk.sahha.android.domain.repository.RemoteRepo

class PostStepDataUseCase(
    val repository: RemoteRepo
) {
    suspend operator fun invoke(
        stepData: List<StepSendDto>,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        repository.postStepData(
            stepData,
            callback
        )
    }
}