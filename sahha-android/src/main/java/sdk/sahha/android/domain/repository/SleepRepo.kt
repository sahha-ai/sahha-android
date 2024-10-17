package sdk.sahha.android.domain.repository

import sdk.sahha.android.domain.model.dto.SleepDto

// Only relates to SleepDto
internal interface SleepRepo {
    suspend fun getSleep(): List<SleepDto>
    suspend fun saveSleep(sleep: List<SleepDto>)
    suspend fun clearSleep(sleep: List<SleepDto>)
    suspend fun clearAllSleep()
}