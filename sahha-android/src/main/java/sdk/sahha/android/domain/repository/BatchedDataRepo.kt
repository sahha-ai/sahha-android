package sdk.sahha.android.domain.repository

import sdk.sahha.android.domain.model.data_log.SahhaDataLog

internal interface BatchedDataRepo {
    suspend fun getBatchedData(): List<SahhaDataLog>
    suspend fun saveBatchedData(data: List<SahhaDataLog>)
    suspend fun deleteBatchedData(data: List<SahhaDataLog>)

    suspend fun deleteAllBatchedData()
}