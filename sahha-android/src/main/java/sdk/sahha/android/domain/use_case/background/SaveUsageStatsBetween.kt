package sdk.sahha.android.domain.use_case.background

import sdk.sahha.android.domain.repository.AppUsageRepo
import sdk.sahha.android.domain.repository.BatchedDataRepo
import java.time.ZonedDateTime
import javax.inject.Inject

internal class SaveUsageStatsBetween @Inject constructor(
    private val batchedDataRepo: BatchedDataRepo,
    private val appUsageRepo: AppUsageRepo,
) {
    suspend operator fun invoke(
        startDateTime: ZonedDateTime,
        endDateTime: ZonedDateTime,
    ) {
        val events = appUsageRepo.getEvents(
            startDateTime.toInstant().toEpochMilli(),
            endDateTime.toInstant().toEpochMilli(),
        )
        batchedDataRepo.saveBatchedData(events)
    }
}