package sdk.sahha.android.data.repository

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.aggregate.AggregationResultGroupedByPeriod
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByDurationRequest
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaResponseHandler
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.TokenBearer
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.local.dao.HealthConnectConfigDao
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.data.mapper.toSleepSendDto
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.worker.post.HealthConnectPostWorker
import sdk.sahha.android.di.DefaultScope
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.internal_enum.CompatibleApps
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.manager.PostChunkManager
import sdk.sahha.android.domain.manager.SahhaAlarmManager
import sdk.sahha.android.domain.model.dto.StepDto
import sdk.sahha.android.domain.model.dto.send.SleepSendDto
import sdk.sahha.android.domain.model.health_connect.HealthConnectQuery
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
import sdk.sahha.android.domain.model.steps.toStepDto
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.source.SahhaSensor
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass

private val tag = "HealthConnectRepoImpl"

class HealthConnectRepoImpl @Inject constructor(
    private val context: Context,
    @DefaultScope private val defaultScope: CoroutineScope,
    @IoScope private val ioScope: CoroutineScope,
    private val chunkManager: PostChunkManager,
    private val permissionManager: PermissionManager,
    private val configRepo: SahhaConfigRepo,
    private val authRepo: AuthRepo,
    private val sensorRepo: SensorRepo,
    private val workManager: WorkManager,
    private val api: SahhaApi,
    private val client: HealthConnectClient?,
    private val sahhaErrorLogger: SahhaErrorLogger,
    private val sahhaTimeManager: SahhaTimeManager,
    private val healthConnectConfigDao: HealthConnectConfigDao,
    private val sahhaAlarmManager: SahhaAlarmManager,
    private val movementDao: MovementDao
) : HealthConnectRepo {
    override val permissions =
        setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(RestingHeartRateRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(SleepStageRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(BloodPressureRecord::class),
            HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        )
    override val successfulQueryTimestamps =
        hashMapOf<String, LocalDateTime>()

    private fun getDataClassFromPermission(
        permission: String
    ): KClass<out Record>? {
        return when (permission) {
            HealthPermission.getReadPermission(HeartRateRecord::class) -> HeartRateRecord::class
            HealthPermission.getReadPermission(RestingHeartRateRecord::class) -> RestingHeartRateRecord::class
            HealthPermission.getReadPermission(StepsRecord::class) -> StepsRecord::class
            HealthPermission.getReadPermission(SleepStageRecord::class) -> SleepStageRecord::class
            HealthPermission.getReadPermission(SleepSessionRecord::class) -> SleepSessionRecord::class
            HealthPermission.getReadPermission(BloodPressureRecord::class) -> BloodPressureRecord::class
            HealthPermission.getReadPermission(BloodGlucoseRecord::class) -> BloodGlucoseRecord::class
            else -> null
        }
    }

    override suspend fun getGrantedPermissions(): Set<String> {
        return client?.permissionController?.getGrantedPermissions() ?: setOf()
    }

    override fun getHealthConnectCompatibleApps(): Set<CompatibleApps> {
        return CompatibleApps.values().toSet()
    }

    override fun startPostWorker(
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        defaultScope.launch {
            val config = configRepo.getConfig()
            if (config.sensorArray.contains(SahhaSensor.device.ordinal)) {
                sensorRepo.startDevicePostWorker(
                    Constants.WORKER_REPEAT_INTERVAL_MINUTES,
                    Constants.DEVICE_POST_WORKER_TAG
                )
            }

            sahhaAlarmManager.setAlarm(
                context,
                Instant.now()
                    .plus(10, ChronoUnit.SECONDS)
                    .toEpochMilli()
            )

            callback?.invoke(
                null,
                true
            )
        }
    }

    private fun startHealthConnectWorker(repeatIntervalMinutes: Long, workerTag: String) {
        val workRequest =
            getWorkRequest<HealthConnectPostWorker>(repeatIntervalMinutes, workerTag, 60)
        startWorkManager(workRequest, workerTag)
    }

    private fun startWorkManager(
        workRequest: PeriodicWorkRequest,
        workerTag: String,
        policy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP
    ) {
        workManager.enqueueUniquePeriodicWork(
            workerTag,
            policy,
            workRequest
        )
    }

    private inline fun <reified T : ListenableWorker> getWorkRequest(
        repeatIntervalMinutes: Long,
        workerTag: String,
        backOffDelaySeconds: Long = 30
    ): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<T>(
            repeatIntervalMinutes,
            TimeUnit.MINUTES
        )
            .addTag(workerTag)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, backOffDelaySeconds, TimeUnit.SECONDS)
            .build()
    }

    private suspend fun getStepResponse(stepData: List<StepDto>): Response<ResponseBody> {
        val token = authRepo.getToken() ?: ""
        return api.postStepData(
            TokenBearer(token),
            stepData
        )
    }

    private suspend fun getSleepSessionResponse(sleepSessionData: List<SleepSendDto>): Response<ResponseBody> {
        val token = authRepo.getToken() ?: ""
        return api.postSleepDataRange(
            TokenBearer(token),
            sleepSessionData
        )
    }

    override suspend fun postSleepSessionData(
        sleepSessionData: List<SleepSessionRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        val getResponse: suspend (List<SleepSessionRecord>) -> Response<ResponseBody> = { chunk ->
            val sleepSessions = chunk.map { it.toSleepSendDto() }
            getSleepSessionResponse(sleepSessions)
        }

        postData(
            sleepSessionData,
            Constants.DEFAULT_POST_LIMIT,
            getResponse,
            {},
            callback
        )
    }

    override suspend fun postStepData(
        stepData: List<StepsHealthConnect>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        val getResponse: suspend (List<StepsHealthConnect>) -> Response<ResponseBody> = { chunk ->
            val steps = chunk.map { it.toStepDto() }
            getStepResponse(steps)
        }

        postData(
            stepData,
            Constants.DEFAULT_POST_LIMIT,
            getResponse,
            {},
            callback
        )
    }

    override suspend fun <T> postData(
        data: List<T>,
        chunkLimit: Int,
        getResponse: suspend (List<T>) -> Response<ResponseBody>,
        clearData: suspend (List<T>) -> Unit,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        try {
            if (data.isEmpty()) {
                callback?.invoke("No data found", false)
                return
            }

            chunkManager.postAllChunks(
                data,
                chunkLimit,
                { chunk ->
                    sendChunk(chunk, getResponse, clearData)
                }
            ) { error, successful ->
                callback?.invoke(error, successful)
            }
        } catch (e: Exception) {
            sensorRepo.handleException(e, "postData", data.toString(), callback)
        }
    }

    private suspend fun <T> sendChunk(
        chunk: List<T>,
        getResponse: suspend (List<T>) -> Response<ResponseBody>,
        clearData: suspend (List<T>) -> Unit,
    ): Boolean {
        return suspendCoroutine { cont ->
            ioScope.launch {
                val response = getResponse(chunk)
                Log.d(tag, "Content length: ${response.raw().request.body?.contentLength()}")

                handleResponse(response, { getResponse(chunk) }, null) {
                    clearData(chunk)
                    cont.resume(true)
                }
            }
        }
    }

    private suspend fun handleResponse(
        response: Response<ResponseBody>,
        retryLogic: suspend (() -> Response<ResponseBody>),
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?,
        successfulLogic: (suspend () -> Unit)? = null
    ) {
        try {
            if (ResponseCode.isUnauthorized(response.code())) {
                callback?.invoke(SahhaErrors.attemptingTokenRefresh, false)
                SahhaResponseHandler.checkTokenExpired(response.code()) {
                    val retryResponse = retryLogic()
                    handleResponse(
                        retryResponse,
                        retryLogic,
                        callback,
                        successfulLogic
                    )
                }
                return
            }

            if (ResponseCode.isSuccessful(response.code())) {
                successfulLogic?.invoke()
                callback?.also {
                    it(null, true)
                }
                return
            }

            callback?.also {
                it(
                    "${response.code()}: ${response.message()}",
                    false
                )
            }

            sahhaErrorLogger.api(response)
        } catch (e: Exception) {
            callback?.also {
                it(e.message, false)
            }

            sahhaErrorLogger.application(
                e.message ?: SahhaErrors.somethingWentWrong,
                tag,
                "handleResponse",
                e.stackTraceToString(),
            )
        }
    }

    override suspend fun getAggregateRecordsByDuration(
        metrics: Set<AggregateMetric<*>>,
        timeRangeFilter: TimeRangeFilter,
        interval: Duration
    ): List<AggregationResultGroupedByDuration>? {
        return client?.aggregateGroupByDuration(
            AggregateGroupByDurationRequest(
                metrics = metrics,
                timeRangeFilter = timeRangeFilter,
                timeRangeSlicer = interval
            )
        )
    }

    override suspend fun getAggregateRecordsByPeriod(
        metrics: Set<AggregateMetric<*>>,
        timeRangeFilter: TimeRangeFilter,
        interval: Period
    ): List<AggregationResultGroupedByPeriod>? {
        return client?.aggregateGroupByPeriod(
            AggregateGroupByPeriodRequest(
                metrics = metrics,
                timeRangeFilter = timeRangeFilter,
                timeRangeSlicer = interval
            )
        )
    }

    override suspend fun <T : Record> getLastSuccessfulQuery(recordType: KClass<T>): LocalDateTime? {
        val permissionString = HealthPermission.getReadPermission(recordType)
        val query = healthConnectConfigDao.getQueryOf(permissionString)
        val instant = query?.let { q ->
            Instant.ofEpochMilli(q.lastSuccessfulTimeStampEpochMillis)
        } ?: return null
        val zoneId = ZoneId.systemDefault().rules.getOffset(instant)

        return LocalDateTime.ofInstant(instant, zoneId)
    }

    override suspend fun <T : Record> saveLastSuccessfulQuery(
        recordType: KClass<T>,
        timeStamp: LocalDateTime
    ) {
        val permissionString = HealthPermission.getReadPermission(recordType)
        val zoneOffset = ZoneId.systemDefault().rules.getOffset(Instant.now())
        val epochMillis = timeStamp.toInstant(zoneOffset).toEpochMilli()

        healthConnectConfigDao.saveQuery(
            HealthConnectQuery(
                permissionString,
                epochMillis
            )
        )
    }

    override suspend fun clearQueries(queries: List<HealthConnectQuery>) {
        healthConnectConfigDao.clearQueries(queries)
    }

    override suspend fun clearAllQueries() {
        healthConnectConfigDao.clearAllQueries()
    }

    override suspend fun <T : Record> getRecords(
        recordType: KClass<T>,
        timeRangeFilter: TimeRangeFilter
    ): List<T>? {
        return client?.readRecords(
            ReadRecordsRequest(
                recordType = recordType,
                timeRangeFilter = timeRangeFilter
            )
        )?.records
    }

    override suspend fun <T : Record> getCurrentDayRecordsSteps(dataType: KClass<T>): List<T>? {
        return if (isFirstQuery(dataType)) runBeforeQuerySteps(dataType)
        else runAfterQuerySteps(dataType)
    }

    override suspend fun <T : Record> getCurrentDayRecords(dataType: KClass<T>): List<T>? {
        return if (isFirstQuery(dataType)) runBeforeQuery(dataType)
        else runAfterQuery(dataType)
    }

    override suspend fun saveStepsHc(stepsHc: StepsHealthConnect) {
        movementDao.saveStepsHc(stepsHc)
    }

    override suspend fun saveStepsListHc(stepsListHc: List<StepsHealthConnect>) {
        movementDao.saveStepsListHc(stepsListHc)
    }

    override suspend fun getAllStepsHc(): List<StepsHealthConnect> {
        return movementDao.getAllStepsHc()
    }

    override suspend fun clearStepsListHc(stepsHc: List<StepsHealthConnect>) {
        movementDao.clearStepsListHc(stepsHc)
    }

    override suspend fun clearAllStepsHc() {
        movementDao.clearAllStepsHc()
    }

    private suspend fun <T : Record> isFirstQuery(dataType: KClass<T>): Boolean {
        return getLastSuccessfulQuery(dataType)?.let { false } ?: true
    }

    private suspend fun <T : Record> runBeforeQuery(dataType: KClass<T>): List<T>? {
        val now = LocalDateTime.now()
        val records = getRecords(
            dataType,
            TimeRangeFilter.before(now)
        )
        if (records.isNullOrEmpty()) return null

        successfulQueryTimestamps[HealthPermission.getReadPermission(dataType)] = now
        return records
    }

    private suspend fun <T : Record> runBeforeQuerySteps(dataType: KClass<T>): List<T>? {
        val now = LocalDateTime.of(
            LocalDate.now(), LocalTime.MIDNIGHT
        )
        val records = getRecords(
            dataType,
            TimeRangeFilter.before(now)
        )
        if (records.isNullOrEmpty()) return null

        successfulQueryTimestamps[HealthPermission.getReadPermission(dataType)] = now
        return records
    }

    private suspend fun <T : Record> runAfterQuery(dataType: KClass<T>): List<T>? {
        val lastQueryTimestamp = getLastSuccessfulQuery(dataType)
        return lastQueryTimestamp?.let { timestamp ->
            val records = getRecords(
                dataType,
                TimeRangeFilter.after(timestamp)
            )
            if (records.isNullOrEmpty()) return@let null

            successfulQueryTimestamps[HealthPermission.getReadPermission(dataType)] =
                LocalDateTime.now()
            return@let records
        }
    }

    private suspend fun <T : Record> runAfterQuerySteps(dataType: KClass<T>): List<T>? {
        val timestamp = getLastQueryTimeStampForSteps(dataType)
        val records = getRecords(
            dataType,
            TimeRangeFilter.after(timestamp)
        )
        if (records.isNullOrEmpty()) return null

        successfulQueryTimestamps[HealthPermission.getReadPermission(dataType)] =
            LocalDateTime.of(
                LocalDate.now(), LocalTime.MIDNIGHT
            )
        return records
    }

    private suspend fun <T : Record> getLastQueryTimeStampForSteps(dataType: KClass<T>): LocalDateTime {
        val timestampDate = getLastSuccessfulQuery(dataType)
            ?.toLocalDate()

        return LocalDateTime.of(timestampDate, LocalTime.MIDNIGHT)
    }
}