package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.repository.RemoteRepo
import javax.inject.Inject

class PostDeviceDataUseCase @Inject constructor(
    private val repository: RemoteRepo
) {
    suspend operator fun invoke(callback: ((error: String?, success: Boolean) -> Unit)?) {
        repository.postPhoneScreenLockData(callback)
    }
}