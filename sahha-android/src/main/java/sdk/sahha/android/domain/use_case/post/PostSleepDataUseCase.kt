package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.repository.RemoteRepo
import javax.inject.Inject

class PostSleepDataUseCase @Inject constructor(
    private val repository: RemoteRepo
) {
    suspend operator fun invoke(callback: ((error: String?, success: String?) -> Unit)?) {
        repository.postSleepData(callback)
    }
}