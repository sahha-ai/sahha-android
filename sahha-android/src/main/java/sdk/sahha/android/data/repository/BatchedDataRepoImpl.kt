package sdk.sahha.android.data.repository

import kotlinx.coroutines.flow.Flow
import sdk.sahha.android.data.local.dao.BatchedDataDao
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.repository.BatchedDataRepo
import javax.inject.Inject

internal class BatchedDataRepoImpl @Inject constructor(
    val dao: BatchedDataDao
): BatchedDataRepo {
    override suspend fun getBatchedData(): List<SahhaDataLog> {
        return dao.getBatchedData()
    }

    override suspend fun saveBatchedData(data: List<SahhaDataLog>) {
        dao.saveBatchedData(data)
    }

    override suspend fun deleteBatchedData(data: List<SahhaDataLog>) {
        dao.deleteBatchedData(data)
    }

    override suspend fun deleteAllBatchedData() {
        dao.deleteAllBatchedData()
    }
}