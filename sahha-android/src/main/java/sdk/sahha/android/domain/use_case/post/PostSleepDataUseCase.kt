package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.repository.SensorRepo
import javax.inject.Inject

class PostSleepDataUseCase @Inject constructor(
    private val repository: SensorRepo,
) {
    suspend operator fun invoke(
        sleepData: List<SleepDto>,
        callback: (suspend (error: String?, success: Boolean) -> Unit)? = null
    ) {
        repository.postSleepData(sleepData, callback)
    }
}