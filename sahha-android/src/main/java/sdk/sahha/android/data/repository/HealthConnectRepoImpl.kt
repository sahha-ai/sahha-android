package sdk.sahha.android.data.repository

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
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
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.worker.post.HealthConnectPostWorker
import sdk.sahha.android.di.DefaultScope
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.internal_enum.CompatibleApps
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.manager.PostChunkManager
import sdk.sahha.android.domain.model.dto.StepDto
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus
import java.time.Duration
import java.time.Instant
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
    private val sahhaTimeManager: SahhaTimeManager
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

    override fun startPostWorker() {
        defaultScope.launch {
            val config = configRepo.getConfig()
            permissionManager.getHealthConnectStatus(context) { _, status ->
                if (config.sensorArray.contains(SahhaSensor.device.ordinal))
                    sensorRepo.startDevicePostWorker(
                        Constants.WORKER_REPEAT_INTERVAL_MINUTES,
                        Constants.DEVICE_POST_WORKER_TAG
                    )

                if (status == SahhaSensorStatus.enabled)
                    startHealthConnectWorker(
                        Constants.WORKER_REPEAT_INTERVAL_MINUTES,
                        Constants.HEALTH_CONNECT_POST_WORKER_TAG
                    )
            }
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

    override suspend fun getAggregateRecords(
        metrics: Set<AggregateMetric<*>>,
        start: Instant,
        end: Instant,
        interval: Duration
    ): List<AggregationResultGroupedByDuration>? {
        return client?.aggregateGroupByDuration(
            AggregateGroupByDurationRequest(
                metrics = metrics,
                timeRangeFilter = TimeRangeFilter.between(start, end),
                timeRangeSlicer = interval
            )
        )
    }

    override suspend fun <T : Record> getRecords(
        recordType: KClass<T>,
        start: Instant,
        end: Instant,
    ): List<T>? {
        return client?.readRecords(
            ReadRecordsRequest(
                recordType = recordType,
                timeRangeFilter = TimeRangeFilter.between(
                    start, end
                )
            )
        )?.records
    }

    override fun toStepDto(record: StepsRecord): StepDto {
        return StepDto(
            dataType = Constants.HEALTH_CONNECT_STEP_DATA_TYPE,
            count = record.count.toInt(),
            source = record.metadata.dataOrigin.packageName,
            manuallyEntered = record.metadata.recordingMethod == 3,
            startDateTime = sahhaTimeManager.instantToIsoTime(record.startTime),
            endDateTime = sahhaTimeManager.instantToIsoTime(record.endTime)
        )
    }
}