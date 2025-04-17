package sdk.sahha.android.data.repository

import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.repository.BatchedDataRepo

internal class MockBatchedDataRepoImpl: BatchedDataRepo {
    private val mockData = mutableListOf<SahhaDataLog>()

    override suspend fun getBatchedData(): List<SahhaDataLog> {
        return mockData
    }

    // Replace if exists
    override suspend fun saveBatchedData(data: List<SahhaDataLog>) {
        data.forEach { toSave ->
            val alreadyExisting = mockData.find { data -> data.id == toSave.id }
            alreadyExisting?.also {
                mockData.add(toSave)
                mockData.remove(alreadyExisting)
            } ?: mockData.add(toSave)
        }
    }

    override suspend fun deleteBatchedData(data: List<SahhaDataLog>) {
        mockData.removeAll(data)
    }

    override suspend fun deleteAllBatchedData() {
        mockData.clear()
    }
}