package sdk.sahha.android.domain.use_case.background

import kotlinx.coroutines.coroutineScope
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.dto.QueryTime
import sdk.sahha.android.domain.provider.PermissionActionProvider
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaStatInterval
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

internal class BatchAggregateLogs @Inject constructor(
    private val timeManager: SahhaTimeManager,
    private val provider: PermissionActionProvider,
    private val repository: HealthConnectRepo
) {
    suspend operator fun invoke(
        sensor: SahhaSensor,
        interval: SahhaStatInterval,
        lastQueryTime: QueryTime,
    ): Pair<String?, List<SahhaDataLog>?> = coroutineScope {
        val now = ZonedDateTime.now()
        val nowIso = timeManager.localDateTimeToISO(now.toLocalDateTime(), now.zone)
        val nowHour = now.truncatedTo(ChronoUnit.HOURS)
        val nowDay = now.truncatedTo(ChronoUnit.DAYS)
        val lastQuery = timeManager.epochMillisToZdt(lastQueryTime.timeEpochMilli)
        val lastQueryHour = lastQuery.truncatedTo(ChronoUnit.HOURS)
        val lastQueryDay = lastQuery.truncatedTo(ChronoUnit.DAYS)

        val intervalIsDay = interval == SahhaStatInterval.day
        val intervalIsHour = interval == SahhaStatInterval.hour
        val isNewHour = lastQueryHour < nowHour
        val isNewDay = lastQueryDay < nowDay

        return@coroutineScope if (intervalIsDay) {
            if (isNewDay) {
                repository.saveCustomSuccessfulQuery(
                    Constants.AGGREGATE_QUERY_ID_DAY,
                    now
                )

                provider.permissionActionsLogs[sensor]?.invoke(
                    Duration.ofDays(1),
                    lastQueryDay,
                    nowDay,
                    nowIso,
                    interval.name
                ) ?: Pair(SahhaErrors.sensorNoStatsFound, null)
            } else Pair(SahhaErrors.sensorNoStatsFound, null)
        } else if (intervalIsHour) {
            if (isNewHour) {
                repository.saveCustomSuccessfulQuery(
                    Constants.AGGREGATE_QUERY_ID_HOUR,
                    now
                )

                provider.permissionActionsLogs[sensor]?.invoke(
                    Duration.ofHours(1),
                    lastQueryHour,
                    nowHour,
                    nowIso,
                    interval.name
                ) ?: Pair(SahhaErrors.sensorNoStatsFound, null)
            } else Pair(SahhaErrors.sensorNoStatsFound, null)
        } else Pair(SahhaErrors.sensorNoStatsFound, null)
    }
}