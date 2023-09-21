package sdk.sahha.android.domain.use_case.query

import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.time.TimeRangeFilter
import sdk.sahha.android.domain.repository.HealthConnectRepo
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.reflect.KClass

class QueryHealthConnectRecordsUseCase @Inject constructor(
    private val repo: HealthConnectRepo
) {
    internal suspend operator fun <T : Record> invoke(dataType: KClass<T>): List<T>? {
        return if (isFirstQuery(dataType)) runBeforeQuery(dataType)
        else runAfterQuery(dataType)
    }

    private suspend fun <T : Record> isFirstQuery(dataType: KClass<T>): Boolean {
        return repo.getLastSuccessfulQuery(dataType)?.let { false } ?: true
    }

    private suspend fun <T : Record> runBeforeQuery(dataType: KClass<T>): List<T>? {
        val now = LocalDateTime.now()
        val records = repo.getRecords(
            dataType,
            TimeRangeFilter.before(now)
        )
        if (records.isNullOrEmpty()) return null

        repo.saveLastSuccessfulQuery(StepsRecord::class, now)
        return records
    }

    private suspend fun <T : Record> runAfterQuery(dataType: KClass<T>): List<T>? {
        val lastQueryTimestamp = repo.getLastSuccessfulQuery(StepsRecord::class)
        return lastQueryTimestamp?.let { timestamp ->
            val records = repo.getRecords(
                dataType,
                TimeRangeFilter.after(timestamp)
            )
            if(records.isNullOrEmpty()) return@let null

            repo.saveLastSuccessfulQuery(StepsRecord::class, LocalDateTime.now())
            return@let records
        }
    }
}