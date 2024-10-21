package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.repository.DeviceUsageRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.use_case.metadata.AddMetadata
import javax.inject.Inject

internal class PostDeviceDataUseCase @Inject constructor(
    private val sensorRepo: SensorRepo,
    private val deviceUsageRepo: DeviceUsageRepo,
    private val addMetadata: AddMetadata
) {
    suspend operator fun invoke(
        lockData: List<PhoneUsage>,
        callback: (suspend (error: String?, success: Boolean) -> Unit)? = null
    ) {
        val metadataAdded = addMetadata(
            dataList = lockData,
            saveData = deviceUsageRepo::saveUsages
        )
        sensorRepo.postPhoneScreenLockData(metadataAdded, callback)
    }
}