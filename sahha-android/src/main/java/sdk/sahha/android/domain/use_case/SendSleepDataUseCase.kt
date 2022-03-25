package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.SleepWorkerRepo
import javax.inject.Inject

class SendSleepDataUseCase @Inject constructor(
    private val repository: SleepWorkerRepo
) {
    suspend operator fun invoke(callback: ((responseSuccessful: Boolean) -> Unit)) {
        repository.postSleepData(callback)
    }
}