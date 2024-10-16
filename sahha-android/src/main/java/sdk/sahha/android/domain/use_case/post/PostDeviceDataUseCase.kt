package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.use_case.AddPhoneUsageMetadata
import sdk.sahha.android.domain.use_case.AddSahhaDataLogMetadata
import javax.inject.Inject

internal class PostDeviceDataUseCase @Inject constructor(
    private val repository: SensorRepo,
    private val addPhoneUsageMetadata: AddPhoneUsageMetadata
) {
    suspend operator fun invoke(
        lockData: List<PhoneUsage>,
        callback: (suspend (error: String?, success: Boolean) -> Unit)? = null
    ) {
        val modified = addPhoneUsageMetadata(lockData)
        repository.postPhoneScreenLockData(modified, callback)
    }
}