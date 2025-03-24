package sdk.sahha.android.domain.use_case.background

import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.provider.PermissionActionProvider
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaStatInterval
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

internal class BatchAggregateLogs @Inject constructor(
    private val timeManager: SahhaTimeManager,
    private val provider: PermissionActionProvider
) {
    suspend operator fun invoke(
        sensor: SahhaSensor,
        interval: SahhaStatInterval,
        dates: Pair<ZonedDateTime, ZonedDateTime>,
    ): Pair<String?, List<SahhaDataLog>?> {
        val now = ZonedDateTime.now()
        val duration =
            if (interval == SahhaStatInterval.day) Duration.ofDays(1)
            else Duration.ofHours(1)

        return provider.permissionActionsLogs[sensor]?.invoke(
            duration,
            dates.first,
            dates.second,
            timeManager.nowInISO(),
            interval.name,
        ) ?: Pair(SahhaErrors.sensorHasNoStats, null)
    }

    suspend fun explicitQuery(
        logs: List<SahhaDataLog>,
        interval: SahhaStatInterval,
    ): List<SahhaDataLog> {
        val hourlyMap = mutableMapOf<String, MutableSet<SahhaSensor>>()
        for (log in logs) {
            val truncated =
                timeManager.localDateTimeToISO(
                    timeManager.ISOToZonedDateTime(log.startDateTime)
                        .truncatedTo(ChronoUnit.HOURS)
                        .toLocalDateTime()
                )

            try {
                hourlyMap.getOrPut(truncated) { mutableSetOf() }
                    .add(SahhaSensor.valueOf(log.dataType))
            } catch (e: Exception) {
                println(e.message)
                continue
            }
        }

        val aggregateLogs = mutableSetOf<SahhaDataLog>()
        val duration = if (interval == SahhaStatInterval.hour) Duration.ofHours(1) else Duration.ofDays(1)
        val postDateTime = timeManager.nowInISO()
        hourlyMap.forEach {
            for (sensor in it.value) {
                val start = timeManager.ISOToZonedDateTime(it.key)
                val end = start.plusHours(1)
                if (end > ZonedDateTime.now()) continue // Ignore incomplete hour

                aggregateLogs += provider.permissionActionsLogs[sensor]?.invoke(
                    duration,
                    start,
                    end,
                    postDateTime,
                    interval.name
                )?.second?.toSet() ?: setOf()
            }
        }

        return aggregateLogs.toList()
    }
}