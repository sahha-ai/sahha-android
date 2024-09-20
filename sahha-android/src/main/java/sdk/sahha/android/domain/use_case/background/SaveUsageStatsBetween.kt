package sdk.sahha.android.domain.use_case.background

import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.repository.AppUsageRepo
import java.time.ZonedDateTime
import javax.inject.Inject

internal class SaveUsageStatsBetween @Inject constructor(
    private val appUsageRepo: AppUsageRepo,
) {
    suspend operator fun invoke(
        startDateTimeEpochMilli: Long,
        endDateTimeEpochMilli: Long,
    ): List<SahhaDataLog> {
        return appUsageRepo.getEvents(
            startDateTimeEpochMilli,
            endDateTimeEpochMilli,
        )
    }
}