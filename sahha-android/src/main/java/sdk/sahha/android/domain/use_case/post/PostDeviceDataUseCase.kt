package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.repository.SensorRepo
import javax.inject.Inject

internal class PostDeviceDataUseCase @Inject constructor(
    private val repository: SensorRepo,
) {
    suspend operator fun invoke(
        lockData: List<PhoneUsage>,
        callback: (suspend (error: String?, success: Boolean) -> Unit)? = null
    ) {
        repository.postPhoneScreenLockData(lockData, callback)
    }
}