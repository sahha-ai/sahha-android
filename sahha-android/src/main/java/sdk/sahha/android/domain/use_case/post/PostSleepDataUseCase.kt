package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.repository.RemotePostRepo
import javax.inject.Inject

class PostSleepDataUseCase @Inject constructor(
    private val repository: RemotePostRepo
) {
    suspend operator fun invoke(callback: ((error: String?, success: String?) -> Unit)?) {
        repository.postSleepData(callback)
    }
}