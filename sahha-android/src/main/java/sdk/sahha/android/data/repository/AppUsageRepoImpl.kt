package sdk.sahha.android.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.os.Build
import sdk.sahha.android.data.local.dao.HealthConnectConfigDao
import sdk.sahha.android.data.mapper.toSahhaDataLog
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.health_connect.HealthConnectQuery
import sdk.sahha.android.domain.repository.AppUsageRepo
import javax.inject.Inject

internal class AppUsageRepoImpl @Inject constructor(
    private val usageStatsManager: UsageStatsManager,
    private val queriedTimeDao: HealthConnectConfigDao
) : AppUsageRepo {

    private val trackedEvents = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        listOf(
            UsageEvents.Event.ACTIVITY_RESUMED,
            UsageEvents.Event.ACTIVITY_PAUSED,
            UsageEvents.Event.DEVICE_STARTUP,
            UsageEvents.Event.DEVICE_SHUTDOWN,
        )
    } else {
        listOf(
            UsageEvents.Event.MOVE_TO_FOREGROUND,
            UsageEvents.Event.MOVE_TO_BACKGROUND,
        )
    }

    override suspend fun storeQueryTime(
        id: String, timestamp: Long
    ) {
        queriedTimeDao.saveQuery(
            HealthConnectQuery(
                id, timestamp
            )
        )
    }

    override suspend fun getQueryTime(id: String): Long? {
        return queriedTimeDao.getQueryOf(id)?.lastSuccessfulTimeStampEpochMillis
    }

    override fun getEvents(
        start: Long, end: Long
    ): List<SahhaDataLog> {
        val events = mutableListOf<UsageEvents.Event>()
        val query = usageStatsManager.queryEvents(start, end)
        while (query.hasNextEvent()) {
            val event = UsageEvents.Event()
            query.getNextEvent(event)


            if (trackedEvents.contains(event.eventType))
                events.add(event)
        }
        return events.map { it.toSahhaDataLog() }
    }
}