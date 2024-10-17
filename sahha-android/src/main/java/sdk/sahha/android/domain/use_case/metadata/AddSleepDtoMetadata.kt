package sdk.sahha.android.domain.use_case.metadata

import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.model.data_log.SahhaMetadata
import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.repository.SleepRepo
import javax.inject.Inject

internal class AddSleepDtoMetadata @Inject constructor(
    private val timeManager: SahhaTimeManager,
    private val repo: SleepRepo
) {
    suspend operator fun invoke(
        sleepData: List<SleepDto>,
        postDateTime: String = timeManager.nowInISO()
    ): List<SleepDto> {
        val modifiedData = mutableListOf<SleepDto>()

        sleepData.forEach { sleep ->
            val metadata = sleep.metadata
            val postDateTimes = metadata?.postDateTime?.toMutableList()

            modifiedData += postDateTimes?.let { times ->
                appendPostDateTime(sleep.copy(metadata = SahhaMetadata(times)), postDateTime)
            } ?: appendPostDateTime(sleep, postDateTime)
        }

        repo.saveSleep(modifiedData)
        return modifiedData
    }

    private fun appendPostDateTime(dataLog: SleepDto, postDateTime: String): SleepDto {
        val postDateTimes = dataLog.metadata?.postDateTime?.toMutableList() ?: mutableListOf()
        postDateTimes.add(postDateTime)
        return dataLog.copy(metadata = SahhaMetadata(postDateTimes))
    }
}