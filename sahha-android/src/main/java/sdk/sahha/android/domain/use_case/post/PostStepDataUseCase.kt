package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.repository.SensorRepo

class PostStepDataUseCase (
    val repository: SensorRepo
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