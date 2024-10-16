package sdk.sahha.android.domain.use_case

import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
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
            val postDateTimes = metadata?.let {
                it[Constants.POST_DATE_TIME] as List<String>
            }

            modifiedData += dataLog.copy(metadata = hashMapOf(Constants.POST_DATE_TIME to postDateTime))
        }

        repo.saveBatchedData(modifiedData)
        return modifiedData
    }
}