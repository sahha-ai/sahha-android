package sdk.sahha.android.domain.use_case

import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.data_log.SahhaMetadata
import sdk.sahha.android.domain.repository.BatchedDataRepo
import javax.inject.Inject

internal class AddSahhaDataLogMetadata @Inject constructor(
    private val timeManager: SahhaTimeManager,
    private val repo: BatchedDataRepo,
) {
    suspend operator fun invoke(
        batchedData: List<SahhaDataLog>,
        postDateTime: String = timeManager.nowInISO()
    ): List<SahhaDataLog> {
        val modifiedData = mutableListOf<SahhaDataLog>()

        batchedData.forEach { dataLog ->
            val metadata = dataLog.metadata
            val postDateTimes = metadata?.postDateTime?.toMutableList()

            modifiedData += postDateTimes?.let { times ->
                appendPostDateTime(dataLog.copy(metadata = SahhaMetadata(times)), postDateTime)
            } ?: appendPostDateTime(dataLog, postDateTime)
        }

        repo.saveBatchedData(modifiedData)
        return modifiedData
    }

    private fun appendPostDateTime(dataLog: SahhaDataLog, postDateTime: String): SahhaDataLog {
        val postDateTimes = dataLog.metadata?.postDateTime?.toMutableList() ?: mutableListOf()
        postDateTimes.add(postDateTime)
        return dataLog.copy(metadata = SahhaMetadata(postDateTimes))
    }
}