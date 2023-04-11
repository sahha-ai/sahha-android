package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.repository.SensorRepo
import javax.inject.Inject

class PostDeviceDataUseCase @Inject constructor (
    private val repository: SensorRepo
) {
    suspend operator fun invoke(callback: ((error: String?, success: Boolean) -> Unit)? = null) {
        repository.postPhoneScreenLockData(callback)
    }
}