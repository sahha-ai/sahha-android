package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.use_case.metadata.AddSleepDtoMetadata
import javax.inject.Inject

internal class PostSleepDataUseCase @Inject constructor(
    private val repository: SensorRepo,
    private val addSleepDtoMetadata: AddSleepDtoMetadata,
) {
    suspend operator fun invoke(
        sleepData: List<SleepDto>,
        callback: (suspend (error: String?, success: Boolean) -> Unit)? = null
    ) {
        val metadataAdded = addSleepDtoMetadata(sleepData)
        repository.postSleepData(metadataAdded, callback)
    }
}