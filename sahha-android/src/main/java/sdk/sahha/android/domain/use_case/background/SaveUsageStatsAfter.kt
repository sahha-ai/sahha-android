package sdk.sahha.android.domain.use_case.background

import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.repository.AppUsageRepo
import java.time.ZonedDateTime
import javax.inject.Inject

internal class SaveUsageStatsAfter @Inject constructor(
    private val appUsageRepo: AppUsageRepo,
) {
    suspend operator fun invoke(
        startDateTimeEpochMilli: Long,
    ): List<SahhaDataLog> {
        val now = ZonedDateTime.now()

        return appUsageRepo.getEvents(
            startDateTimeEpochMilli,
            now.toInstant().toEpochMilli(),
        )
    }
}