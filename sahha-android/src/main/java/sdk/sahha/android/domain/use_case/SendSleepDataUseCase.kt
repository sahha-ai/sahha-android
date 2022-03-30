package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.RemotePostRepo
import javax.inject.Inject

class SendSleepDataUseCase @Inject constructor(
    private val repository: RemotePostRepo
) {
    suspend operator fun invoke(callback: ((responseSuccessful: Boolean) -> Unit)) {
        repository.postSleepData(callback)
    }
}