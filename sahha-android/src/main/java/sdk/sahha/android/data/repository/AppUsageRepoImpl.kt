package sdk.sahha.android.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import kotlinx.coroutines.CoroutineScope
import sdk.sahha.android.common.Constants
import sdk.sahha.android.data.local.dao.HealthConnectConfigDao
import sdk.sahha.android.data.mapper.UsageEventMapper
import sdk.sahha.android.data.mapper.toSahhaDataLog
import sdk.sahha.android.di.DefaultScope
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.health_connect.HealthConnectQuery
import sdk.sahha.android.domain.repository.AppUsageRepo
import javax.inject.Inject

internal class AppUsageRepoImpl @Inject constructor(
    private val usageStatsManager: UsageStatsManager,
    private val queriedTimeDao: HealthConnectConfigDao,
    @DefaultScope private val scope: CoroutineScope
) : AppUsageRepo {
    private val eventTypeKey = "eventType"
    private val sourceAndroid = "android"
    private val sourceNexusLauncher = "com.google.android.apps.nexuslauncher"

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

    override suspend fun getEvents(
        start: Long, end: Long
    ): List<SahhaDataLog> {
        val events = mutableListOf<UsageEvents.Event>()
        val query = usageStatsManager.queryEvents(start, end)
        while (query.hasNextEvent()) {
            val event = UsageEvents.Event()
            query.getNextEvent(event)
            events.add(event)
        }

        val logs = events.map { it.toSahhaDataLog() }

        return filterEventLogs(logs)
    }

    override fun filterEventLogs(
        logs: List<SahhaDataLog>
    ): List<SahhaDataLog> {
        return logs.filterNot {
            eventTypeUnknown(it) ||
                    packageNameUnknown(it) ||
                    eventTypeNone(it) ||
                    eventTypeStandByBucket(it) ||
                    sourceAndroid(it) ||
                    sourceNexusLauncher(it)
        }
    }

    private fun eventTypeStandByBucket(log: SahhaDataLog): Boolean {
        return log.additionalProperties?.get(eventTypeKey)
            ?.let { type -> type == "STANDBY_BUCKET_CHANGED" }
            ?: false
    }

    private fun sourceAndroid(log: SahhaDataLog): Boolean {
        return log.source == sourceAndroid
    }

    private fun sourceNexusLauncher(log: SahhaDataLog): Boolean {
        return log.source == sourceNexusLauncher
    }

    private fun eventTypeNone(log: SahhaDataLog): Boolean {
        return log.additionalProperties?.get(eventTypeKey)
            ?.let { type -> type == UsageEventMapper.getString(UsageEvents.Event.NONE) }
            ?: false
    }

    private fun packageNameUnknown(log: SahhaDataLog): Boolean {
        return log.source == Constants.UNKNOWN
    }

    private fun eventTypeUnknown(log: SahhaDataLog): Boolean {
        return log.additionalProperties?.get("eventType")?.let { type -> type == Constants.UNKNOWN }
            ?: false
    }
}