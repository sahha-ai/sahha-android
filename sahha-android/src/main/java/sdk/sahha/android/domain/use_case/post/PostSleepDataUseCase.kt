package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.repository.SleepRepo
import sdk.sahha.android.domain.use_case.metadata.AddMetadata
import javax.inject.Inject

internal class PostSleepDataUseCase @Inject constructor(
    private val sensorRepo: SensorRepo,
    private val sleepRepo: SleepRepo,
    private val addMetadata: AddMetadata,
) {
    suspend operator fun invoke(
        sleepData: List<SleepDto>,
        callback: (suspend (error: String?, success: Boolean) -> Unit)? = null
    ) {
        val metadataAdded = addMetadata(
            dataList = sleepData,
            saveData = sleepRepo::saveSleep
        )
        sensorRepo.postSleepData(metadataAdded, callback)
    }
}