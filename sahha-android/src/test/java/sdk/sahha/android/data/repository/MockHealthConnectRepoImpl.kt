package sdk.sahha.android.data.repository

import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.aggregate.AggregationResultGroupedByPeriod
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.testing.AggregationResult
import androidx.health.connect.client.testing.FakeHealthConnectClient
import androidx.health.connect.client.time.TimeRangeFilter
import okhttp3.ResponseBody
import retrofit2.Response
import sdk.sahha.android.domain.internal_enum.CompatibleApps
import sdk.sahha.android.domain.model.health_connect.HealthConnectChangeToken
import sdk.sahha.android.domain.model.health_connect.HealthConnectQuery
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
import sdk.sahha.android.domain.repository.HealthConnectRepo
import java.time.Duration
import java.time.LocalDateTime
import java.time.Period
import java.time.ZonedDateTime
import kotlin.reflect.KClass

internal class MockHealthConnectRepoImpl: HealthConnectRepo {
    private val steps = mutableListOf<StepsHealthConnect>()
    private val queries = hashMapOf<String, ZonedDateTime>()
    override val permissions: Set<String>
        get() = TODO("Not yet implemented")

    override val successfulQueryTimestamps: HashMap<String, ZonedDateTime>
        get() = queries

    override fun getHealthConnectCompatibleApps(): Set<CompatibleApps> {
        TODO("Not yet implemented")
    }

    override suspend fun getGrantedPermissions(): Set<String> {
        val client = FakeHealthConnectClient()
        return client.permissionController.getGrantedPermissions()
    }

    override suspend fun <T> postData(
        data: List<T>,
        chunkLimit: Int,
        getResponse: suspend (List<T>) -> Response<ResponseBody>,
        updateLastQueried: suspend (List<T>) -> Unit,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override fun startDevicePostWorker(callback: ((error: String?, successful: Boolean) -> Unit)?) {
        TODO("Not yet implemented")
    }

    override suspend fun <T : Record> getRecords(
        recordType: KClass<T>,
        timeRangeFilter: TimeRangeFilter
    ): List<T>? {
        TODO("Not yet implemented")
    }

    override suspend fun getAggregateRecordsByDuration(
        metrics: Set<AggregateMetric<*>>,
        timeRangeFilter: TimeRangeFilter,
        interval: Duration
    ): List<AggregationResultGroupedByDuration>? {
        // Create a fake result.
        val now = ZonedDateTime.now()
        val singleSource =
            AggregationResult(
                dataOrigins = setOf(DataOrigin("TEST_PACKAGE_NAME_1")),
                metrics =
                buildMap {
                    put(HeartRateRecord.BPM_AVG, 74.0)
                    put(HeartRateRecord.BPM_AVG, 74L)
                    put(
                        ExerciseSessionRecord.EXERCISE_DURATION_TOTAL,
                        Duration.ofMinutes(30)
                    )
                }
            )
        val multipleSources =
            AggregationResult(
                dataOrigins = setOf(
                    DataOrigin("TEST_PACKAGE_NAME_1"),
                    DataOrigin("TEST_PACKAGE_NAME_2")
                ),
                metrics =
                buildMap {
                    put(HeartRateRecord.BPM_AVG, 99.0)
                    put(HeartRateRecord.BPM_AVG, 99L)
                    put(
                        ExerciseSessionRecord.EXERCISE_DURATION_TOTAL,
                        Duration.ofMinutes(30)
                    )
                }
            )
        val noSource =
            AggregationResult(
                metrics = buildMap {
                    put(HeartRateRecord.BPM_AVG, 123.4)
                    put(HeartRateRecord.BPM_AVG, 123L)
                    put(
                        ExerciseSessionRecord.EXERCISE_DURATION_TOTAL,
                        Duration.ofMinutes(30)
                    )
                }
            )

        return buildList {
            add(
                AggregationResultGroupedByDuration(
                    singleSource,
                    now.minusMinutes(30).toInstant(),
                    now.toInstant(),
                    now.offset
                )
            )
            add(
                AggregationResultGroupedByDuration(
                    multipleSources,
                    now.minusMinutes(60).toInstant(),
                    now.minusMinutes(30).toInstant(),
                    now.offset
                )
            )
            add(
                AggregationResultGroupedByDuration(
                    noSource,
                    now.minusMinutes(90).toInstant(),
                    now.minusMinutes(60).toInstant(),
                    now.offset
                )
            )
        }
    }

    override suspend fun getAggregateRecordsByPeriod(
        metrics: Set<AggregateMetric<*>>,
        timeRangeFilter: TimeRangeFilter,
        interval: Period
    ): List<AggregationResultGroupedByPeriod>? {
        TODO("Not yet implemented")
    }

    override suspend fun <T : Record> getLastSuccessfulQuery(recordType: KClass<T>): ZonedDateTime? {
        return recordType.qualifiedName?.let {
            queries[it]
        }
    }

    override suspend fun <T : Record> saveLastSuccessfulQuery(
        recordType: KClass<T>,
        timeStamp: ZonedDateTime
    ) {
        recordType.qualifiedName?.also {
            queries[it] = timeStamp
        }
    }

    override suspend fun clearQueries(queries: List<HealthConnectQuery>) {
        TODO("Not yet implemented")
    }

    override suspend fun clearAllQueries() {
        TODO("Not yet implemented")
    }

    override suspend fun saveStepsHc(stepsHc: StepsHealthConnect) {
        steps += stepsHc
    }

    override suspend fun saveStepsListHc(stepsListHc: List<StepsHealthConnect>) {
        TODO("Not yet implemented")
    }

    override suspend fun getAllStepsHc(): List<StepsHealthConnect> {
        return steps
    }

    override suspend fun clearStepsListHc(stepsHc: List<StepsHealthConnect>) {
        TODO("Not yet implemented")
    }

    override suspend fun clearAllStepsHc() {
        TODO("Not yet implemented")
    }

    override suspend fun saveChangeToken(changeToken: HealthConnectChangeToken) {
        TODO("Not yet implemented")
    }

    override suspend fun <T : Record> getNewRecords(dataType: KClass<T>): List<T>? {
        TODO("Not yet implemented")
    }

    override suspend fun postStepData(
        stepData: List<StepsHealthConnect>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun postSleepSessionData(
        sleepSessionData: List<SleepSessionRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun postHeartRateVariabilityRmssdData(
        heartRateVariabilityRmssdData: List<HeartRateVariabilityRmssdRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun postBloodGlucoseData(
        bloodGlucoseData: List<BloodGlucoseRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun postBloodPressureData(
        bloodPressureData: List<BloodPressureRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun <T : Record> getCurrentDayRecords(dataType: KClass<T>): List<T>? {
        TODO("Not yet implemented")
    }

    override suspend fun clearStepsBeforeHc(dateTime: LocalDateTime) {
        TODO("Not yet implemented")
    }

    override suspend fun postHeartRateData(
        heartRateData: List<HeartRateRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun postRestingHeartRateData(
        restingHeartRateData: List<RestingHeartRateRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun postActiveEnergyBurned(
        activeCalBurnedData: List<ActiveCaloriesBurnedRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun postTotalEnergyBurned(
        totalCaloriesBurnedData: List<TotalCaloriesBurnedRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun postOxygenSaturation(
        oxygenSaturationData: List<OxygenSaturationRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun postVo2MaxData(
        vo2MaxData: List<Vo2MaxRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun saveCustomSuccessfulQuery(customId: String, timeStamp: ZonedDateTime) {
        queries[customId] = timeStamp
    }

    override suspend fun getLastCustomQuery(customId: String): HealthConnectQuery? {
        return queries[customId]?.let {
            HealthConnectQuery(customId, it.toInstant().toEpochMilli())
        }
    }

    override suspend fun <T : Record> getExistingChangesToken(recordType: KClass<T>): String? {
        TODO("Not yet implemented")
    }

    override suspend fun <T : Record> getChangedRecords(
        recordType: KClass<T>,
        token: String?
    ): List<Record>? {
        TODO("Not yet implemented")
    }

    override suspend fun clearAllChangeTokens() {
        TODO("Not yet implemented")
    }

    override var shouldLoop: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun resetHasMore() {
        TODO("Not yet implemented")
    }
}