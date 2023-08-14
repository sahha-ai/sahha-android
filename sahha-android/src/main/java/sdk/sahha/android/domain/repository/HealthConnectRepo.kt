package sdk.sahha.android.domain.repository

import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord
import okhttp3.ResponseBody
import retrofit2.Response
import sdk.sahha.android.domain.internal_enum.CompatibleApps
import java.time.Instant

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

    suspend fun getHourlySteps(start: Instant, end: Instant): List<AggregationResultGroupedByDuration>?
    fun getSteps(): List<StepsRecord>?
    fun getSleepSessions(): List<SleepSessionRecord>?
    fun getSleepStages(): List<SleepStageRecord>?
    fun getHeartRate(): List<HeartRateRecord>?
    fun getRestingHeartRate(): List<RestingHeartRateRecord>?
    fun getBloodGlucose(): List<BloodGlucoseRecord>?
    fun getBloodPressure(): List<BloodPressureRecord>?
    suspend fun getHourlySleepSessions(
        start: Instant,
        end: Instant
    ): List<AggregationResultGroupedByDuration>?
}