package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.repository.RemoteRepo

class PostDeviceDataUseCase (
    private val repository: RemoteRepo
) {
    suspend operator fun invoke(callback: ((error: String?, success: Boolean) -> Unit)?) {
        repository.postPhoneScreenLockData(callback)
    }
}