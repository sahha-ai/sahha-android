package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.repository.SensorRepo
import javax.inject.Inject

internal class PostStepDataUseCase @Inject constructor(
    private val repository: SensorRepo,
) {
    suspend operator fun invoke(
        stepData: List<StepData>,
        callback: (suspend (error: String?, success: Boolean) -> Unit)? = null
    ) {
        repository.postStepData(stepData, callback)
    }
}