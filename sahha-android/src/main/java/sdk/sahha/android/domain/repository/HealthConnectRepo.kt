package sdk.sahha.android.domain.repository

import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.aggregate.AggregationResultGroupedByPeriod
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.time.TimeRangeFilter
import okhttp3.ResponseBody
import retrofit2.Response
import sdk.sahha.android.domain.internal_enum.CompatibleApps
import sdk.sahha.android.domain.model.health_connect.HealthConnectQuery
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
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

    fun startPostWorker(
        callback: ((error: String?, successful: Boolean) -> Unit)? = null
    )

    suspend fun <T : Record> getRecords(
        recordType: KClass<T>,
        timeRangeFilter: TimeRangeFilter
    ): List<T>?

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
    suspend fun saveStepsHc(stepsHc: StepsHealthConnect)
    suspend fun saveStepsListHc(stepsListHc: List<StepsHealthConnect>)
    suspend fun getAllStepsHc(): List<StepsHealthConnect>
    suspend fun clearStepsListHc(stepsHc: List<StepsHealthConnect>)
    suspend fun clearAllStepsHc()
    suspend fun <T : Record> getNewRecords(dataType: KClass<T>): List<T>?
    suspend fun postStepData(
        stepData: List<StepsHealthConnect>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    val successfulQueryTimestamps: HashMap<String, LocalDateTime>
    suspend fun postSleepSessionData(
        sleepSessionData: List<SleepSessionRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun <T : Record> postHeartRateAggregateData(
        heartRateAggregateData: List<AggregationResultGroupedByDuration>,
        recordType: KClass<T>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun postHeartRateVariabilityRmssdData(
        heartRateVariabilityRmssdData: List<HeartRateVariabilityRmssdRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun postBloodGlucoseData(
        bloodGlucoseData: List<BloodGlucoseRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun postBloodPressureData(
        bloodPressureData: List<BloodPressureRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun <T : Record> getCurrentDayRecords(dataType: KClass<T>): List<T>?
    suspend fun clearStepsBeforeHc(dateTime: LocalDateTime)
}