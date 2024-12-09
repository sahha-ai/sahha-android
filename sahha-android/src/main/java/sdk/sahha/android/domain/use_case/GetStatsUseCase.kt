package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.model.stats.SahhaStat
import sdk.sahha.android.domain.provider.PermissionActionProvider
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaStatInterval
import java.time.Duration
import java.time.ZonedDateTime
import javax.inject.Inject

internal class GetStatsUseCase @Inject constructor(
    private val provider: PermissionActionProvider,
) {
    suspend operator fun invoke(
        sensor: SahhaSensor,
        interval: SahhaStatInterval,
        startDateTime: ZonedDateTime,
        endDateTime: ZonedDateTime
    ): List<SahhaStat>? {
        val duration =
            if (interval == SahhaStatInterval.day) Duration.ofDays(1)
            else Duration.ofHours(1)

        return provider.permissionActions[sensor]?.invoke(
            duration,
            startDateTime,
            endDateTime
        )
    }
}