package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.repository.SensorRepo
import javax.inject.Inject

class PostStepDataUseCase @Inject constructor  (
    val repository: SensorRepo
) {
    suspend operator fun invoke(
        stepData: List<StepData>,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        repository.postStepData(
            stepData,
            callback
        )
    }
}