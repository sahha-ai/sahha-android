package sdk.sahha.android.data.repository

import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.repository.SleepRepo

internal class MockSleepRepoImpl: SleepRepo {
    private val sleepData = mutableListOf<SleepDto>()

    override suspend fun getSleep(): List<SleepDto> {
        return sleepData
    }

    override suspend fun saveSleep(sleep: List<SleepDto>) {
        sleepData += sleep
    }

    override suspend fun clearSleep(sleep: List<SleepDto>) {
        sleepData.removeAll(sleep)
    }

    override suspend fun clearAllSleep() {
        sleepData.clear()
    }
}