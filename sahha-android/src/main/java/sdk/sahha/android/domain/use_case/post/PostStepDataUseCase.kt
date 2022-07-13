package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.repository.RemoteRepo

class PostStepDataUseCase (
    val repository: RemoteRepo
) {
    suspend operator fun invoke(
        stepData: List<StepData>,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        repository.postStepData(
            stepData,
            callback
        )
    }
}