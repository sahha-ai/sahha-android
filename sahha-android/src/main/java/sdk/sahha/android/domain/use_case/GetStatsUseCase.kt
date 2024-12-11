package sdk.sahha.android.domain.use_case

import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.toMidnight
import sdk.sahha.android.domain.model.local_logs.SahhaStat
import sdk.sahha.android.domain.provider.PermissionActionProvider
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaStatInterval
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import javax.inject.Inject

internal class GetStatsUseCase @Inject constructor(
    private val provider: PermissionActionProvider,
) {
    suspend operator fun invoke(
        sensor: SahhaSensor,
        interval: SahhaStatInterval,
        localDates: Pair<LocalDateTime, LocalDateTime>? = null,
        dates: Pair<Date, Date>? = null,
    ): Pair<String?, List<SahhaStat>?> {
        val zoneId = ZoneId.systemDefault()
        val duration =
            if (interval == SahhaStatInterval.day) Duration.ofDays(1)
            else Duration.ofHours(1)

        return localDates?.let { getStatsLocalDates(sensor, duration, it, zoneId) }
            ?: dates?.let { getStatsDates(sensor, duration, it, zoneId) }
            ?: if (interval == SahhaStatInterval.day) getStatsDailyWeek(sensor, duration, zoneId)
            else getStatsHourlyDay(sensor, duration, zoneId)
    }

    private suspend fun getStatsHourlyDay(
        sensor: SahhaSensor,
        duration: Duration,
        zoneId: ZoneId
    ) = provider.permissionActionsStats[sensor]?.invoke(
        duration,
        ZonedDateTime.of(
            LocalDateTime.of(
                LocalDate.now(),
                LocalTime.MIDNIGHT
            ),
            zoneId
        ),
        ZonedDateTime.of(
            LocalDateTime.of(
                LocalDate.now().plusDays(1),
                LocalTime.MIDNIGHT
            ),
            zoneId
        )
    ) ?: Pair(SahhaErrors.sensorHasNoStats, null)

    private suspend fun getStatsDailyWeek(
        sensor: SahhaSensor,
        duration: Duration,
        zoneId: ZoneId
    ) = provider.permissionActionsStats[sensor]?.invoke(
        duration,
        ZonedDateTime.of(
            LocalDateTime.of(
                LocalDate.now().minusWeeks(1),
                LocalTime.MIDNIGHT
            ),
            zoneId
        ),
        ZonedDateTime.of(
            LocalDateTime.of(
                LocalDate.now().plusDays(1),
                LocalTime.MIDNIGHT
            ),
            zoneId
        )
    ) ?: Pair(
        SahhaErrors.sensorHasNoStats,
        null
    )

    private suspend fun getStatsLocalDates(
        sensor: SahhaSensor,
        duration: Duration,
        it: Pair<LocalDateTime, LocalDateTime>,
        zoneId: ZoneId
    ) = provider.permissionActionsStats[sensor]?.invoke(
        duration,
        ZonedDateTime.of(it.first, zoneId).toMidnight(),
        ZonedDateTime.of(it.second, zoneId).toMidnight(1)
    ) ?: Pair(SahhaErrors.sensorHasNoStats, null)

    private suspend fun getStatsDates(
        sensor: SahhaSensor,
        duration: Duration,
        it: Pair<Date, Date>,
        zoneId: ZoneId
    ) = provider.permissionActionsStats[sensor]?.invoke(
        duration,
        ZonedDateTime.ofInstant(it.first.toInstant(), zoneId).toMidnight(),
        ZonedDateTime.ofInstant(it.second.toInstant(), zoneId).toMidnight(1)
    ) ?: Pair(SahhaErrors.sensorHasNoStats, null)
}