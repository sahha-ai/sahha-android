package sdk.sahha.android.domain.repository

import sdk.sahha.android.domain.model.data_log.SahhaDataLog

internal interface AppUsageRepo {
    fun getEvents(start: Long, end: Long): List<SahhaDataLog>
    suspend fun storeQueryTime(id: String, timestamp: Long)
    suspend fun getQueryTime(id: String): Long?
}