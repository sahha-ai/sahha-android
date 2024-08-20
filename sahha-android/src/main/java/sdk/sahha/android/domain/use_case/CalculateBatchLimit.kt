package sdk.sahha.android.domain.use_case

import kotlinx.coroutines.runBlocking
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.repository.BatchedDataRepo
import sdk.sahha.android.source.SahhaConverterUtility
import javax.inject.Inject

internal class CalculateBatchLimit @Inject constructor(
    private val batchedDataRepo: BatchedDataRepo,
) {
    operator fun invoke(
        batchedData: List<SahhaDataLog> = runBlocking { batchedDataRepo.getBatchedData() },
        chunkBytes: Int = Constants.DATA_LOG_LIMIT_BYTES
    ): Int {
        val avgLogSize = 210
        if (batchedData.isEmpty()) return chunkBytes / avgLogSize

        val sample = batchedData.random()
        val approximateBytesPerLog =
            SahhaConverterUtility.convertToJsonString(sample).toByteArray().size
        return chunkBytes / approximateBytesPerLog
    }
}