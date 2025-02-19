package sdk.sahha.android.domain.repository

import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.aggregate.AggregationResultGroupedByPeriod
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.time.TimeRangeFilter
import okhttp3.ResponseBody
import retrofit2.Response
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.CompatibleApps
import sdk.sahha.android.domain.model.health_connect.HealthConnectChangeToken
import sdk.sahha.android.domain.model.health_connect.HealthConnectQuery
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
import java.time.Duration
import java.time.LocalDateTime
import java.time.Period
import java.time.ZonedDateTime
import kotlin.reflect.KClass

internal interface HealthConnectRepo {
    val permissions: Set<String>
    val successfulQueryTimestamps: HashMap<String, ZonedDateTime>
    fun getHealthConnectCompatibleApps(): Set<CompatibleApps>
    suspend fun getGrantedPermissions(): Set<String>

    suspend fun <T> postData(
        data: List<T>,
        chunkLimit: Int = Constants.DEFAULT_POST_LIMIT,
        getResponse: suspend (List<T>) -> Response<ResponseBody>,
        updateLastQueried: suspend (List<T>) -> Unit = {},
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    fun startDevicePostWorker(
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
    ): ZonedDateTime?

    suspend fun <T : Record> saveLastSuccessfulQuery(
        recordType: KClass<T>,
        timeStamp: ZonedDateTime
    )
    suspend fun clearQueries(queries: List<HealthConnectQuery>)
    suspend fun clearAllQueries()
    suspend fun saveStepsHc(stepsHc: StepsHealthConnect)
    suspend fun saveStepsListHc(stepsListHc: List<StepsHealthConnect>)
    suspend fun getAllStepsHc(): List<StepsHealthConnect>
    suspend fun clearStepsListHc(stepsHc: List<StepsHealthConnect>)
    suspend fun clearAllStepsHc()
    suspend fun saveChangeToken(changeToken: HealthConnectChangeToken)
    suspend fun <T : Record> getNewRecords(dataType: KClass<T>): List<T>?

    suspend fun postStepData(
        stepData: List<StepsHealthConnect>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )
    suspend fun postSleepSessionData(
        sleepSessionData: List<SleepSessionRecord>,
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
    suspend fun postHeartRateData(
        heartRateData: List<HeartRateRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun postRestingHeartRateData(
        restingHeartRateData: List<RestingHeartRateRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun postActiveEnergyBurned(
        activeCalBurnedData: List<ActiveCaloriesBurnedRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun postTotalEnergyBurned(
        totalCaloriesBurnedData: List<TotalCaloriesBurnedRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun postOxygenSaturation(
        oxygenSaturationData: List<OxygenSaturationRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun postVo2MaxData(
        vo2MaxData: List<Vo2MaxRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun saveCustomSuccessfulQuery(customId: String, timeStamp: ZonedDateTime)
    suspend fun getLastCustomQuery(customId: String): HealthConnectQuery?
    suspend fun <T: Record> getExistingChangesToken(recordType: KClass<T>): String?
    suspend fun <T: Record> getChangedRecords(recordType: KClass<T>, token: String? = null): List<Record>?
    suspend fun clearAllChangeTokens()
    var shouldLoop: Boolean
    fun resetHasMore()
}