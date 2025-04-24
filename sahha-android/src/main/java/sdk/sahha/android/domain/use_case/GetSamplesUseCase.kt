package sdk.sahha.android.domain.use_case

import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.toMidnight
import sdk.sahha.android.domain.model.local_logs.SahhaSample
import sdk.sahha.android.domain.provider.PermissionActionProvider
import sdk.sahha.android.source.SahhaSensor
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import javax.inject.Inject

internal class GetSamplesUseCase @Inject constructor(
    private val provider: PermissionActionProvider
) {
    suspend operator fun invoke(
        sensor: SahhaSensor,
        localDates: Pair<LocalDateTime, LocalDateTime>? = null,
        dates: Pair<Date, Date>? = null,
    ): Pair<String?, List<SahhaSample>?> {
        val zoneId = ZoneId.systemDefault()

        return localDates?.let { getSamplesLocalDates(sensor, it, zoneId) }
            ?: dates?.let { getSamplesDates(sensor, it, zoneId) }
            ?: getSamplesForToday(sensor)
    }

    private suspend fun getSamplesLocalDates(
        sensor: SahhaSensor,
        it: Pair<LocalDateTime, LocalDateTime>,
        zoneId: ZoneId
    ) = provider.permissionActionsSamples[sensor]?.invoke(
        ZonedDateTime.of(it.first, zoneId),
        ZonedDateTime.of(it.second, zoneId)
    ) ?: Pair(SahhaErrors.sensorSamplesNotSupported, null)

    private suspend fun getSamplesDates(
        sensor: SahhaSensor,
        it: Pair<Date, Date>,
        zoneId: ZoneId
    ) = provider.permissionActionsSamples[sensor]?.invoke(
        ZonedDateTime.ofInstant(it.first.toInstant(), zoneId),
        ZonedDateTime.ofInstant(it.second.toInstant(), zoneId)
    ) ?: Pair(SahhaErrors.sensorSamplesNotSupported, null)

    private suspend fun getSamplesForToday(
        sensor: SahhaSensor,
    ) = provider.permissionActionsSamples[sensor]?.invoke(
        ZonedDateTime.now().toMidnight(),
        ZonedDateTime.now().toMidnight(1).minusNanos(1)
    ) ?: Pair(SahhaErrors.sensorSamplesNotSupported, null)
}