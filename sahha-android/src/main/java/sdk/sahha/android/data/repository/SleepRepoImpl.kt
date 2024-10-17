package sdk.sahha.android.data.repository

import sdk.sahha.android.data.local.dao.SleepDao
import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.repository.SleepRepo
import javax.inject.Inject

// Only relates to SleepDto
internal class SleepRepoImpl @Inject constructor(
    private val dao: SleepDao
) : SleepRepo {
    override suspend fun getSleep(): List<SleepDto> {
        return dao.getSleepDto()
    }

    override suspend fun saveSleep(sleep: List<SleepDto>) {
        // Local sleep data was originally designed to save one at a time...
        sleep.forEach {
            dao.saveSleepDto(it)
        }
    }

    override suspend fun clearSleep(sleep: List<SleepDto>) {
        dao.clearSleepDto(sleep)
    }

    override suspend fun clearAllSleep() {
        dao.clearSleepDto()
    }
}