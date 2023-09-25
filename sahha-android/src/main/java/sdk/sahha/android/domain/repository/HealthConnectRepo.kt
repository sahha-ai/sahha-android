package sdk.sahha.android.domain.repository

import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.aggregate.AggregationResultGroupedByPeriod
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.time.TimeRangeFilter
import okhttp3.ResponseBody
import retrofit2.Response
import sdk.sahha.android.domain.internal_enum.CompatibleApps
import sdk.sahha.android.domain.model.dto.StepDto
import sdk.sahha.android.domain.model.health_connect.HealthConnectQuery
import java.time.Duration
import java.time.LocalDateTime
import java.time.Period
import kotlin.reflect.KClass

interface HealthConnectRepo {
    val permissions: Set<String>
    fun getHealthConnectCompatibleApps(): Set<CompatibleApps>
    suspend fun getGrantedPermissions(): Set<String>
    suspend fun <T> postData(
        data: List<T>,
        chunkLimit: Int,
        getResponse: suspend (List<T>) -> Response<ResponseBody>,
        clearData: suspend (List<T>) -> Unit,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    fun startPostWorker()
    suspend fun <T : Record> getRecords(
        recordType: KClass<T>,
        timeRangeFilter: TimeRangeFilter
    ): List<T>?

    fun stepRecordToStepDto(record: StepsRecord): StepDto
    suspend fun getAggregateRecordsByDuration(
        metrics: Set<AggregateMetric<*>>,
        timeRangeFilter: TimeRangeFilter,
        interval: Duration
    ): List<AggregationResultGroupedByDuration>?

    suspend fun getAggregateRecordsByPeriod(
        metrics: Set<AggregateMetric<*>>,
        timeRangeFilter: TimeRangeFilter,
        interval: Period
    ): List<AggregationResultGroupedByPeriod>?

    suspend fun <T : Record> getLastSuccessfulQuery(
        recordType: KClass<T>
    ): LocalDateTime?

    suspend fun <T : Record> saveLastSuccessfulQuery(
        recordType: KClass<T>,
        timeStamp: LocalDateTime
    )

    suspend fun clearQueries(queries: List<HealthConnectQuery>)

    suspend fun clearAllQueries()
    suspend fun <T : Record> getCurrentDayRecords(dataType: KClass<T>): List<T>?
}