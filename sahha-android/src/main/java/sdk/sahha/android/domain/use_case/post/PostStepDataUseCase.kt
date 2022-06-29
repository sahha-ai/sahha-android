package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.repository.RemoteRepo
import javax.inject.Inject

class PostStepDataUseCase @Inject constructor(
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