package sdk.sahha.android.domain.repository

import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.records.Record
import okhttp3.ResponseBody
import retrofit2.Response
import sdk.sahha.android.domain.internal_enum.CompatibleApps
import java.time.Instant
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
    suspend fun getHourlyRecords(
        metrics: Set<AggregateMetric<*>>,
        start: Instant,
        end: Instant,
        intervalHours: Long = 1
    ): List<AggregationResultGroupedByDuration>?

    suspend fun <T : Record> getRecords(
        recordType: KClass<T>,
        start: Instant,
        end: Instant
    ): List<T>?
}