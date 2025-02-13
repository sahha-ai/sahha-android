package sdk.sahha.android.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.aggregate.AggregationResultGroupedByPeriod
import androidx.health.connect.client.changes.UpsertionChange
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.request.AggregateGroupByDurationRequest
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.ChangesResponse
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.ResponseBody
import retrofit2.Response
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaResponseHandler
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.Session
import sdk.sahha.android.common.TokenBearer
import sdk.sahha.android.data.local.dao.HealthConnectConfigDao
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.data.mapper.toBloodPressureDiastolic
import sdk.sahha.android.data.mapper.toBloodPressureSystolic
import sdk.sahha.android.data.mapper.toSahhaDataLogAsChildLog
import sdk.sahha.android.data.mapper.toSahhaDataLogDto
import sdk.sahha.android.data.mapper.toSahhaLogDto
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.di.DefaultScope
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.internal_enum.CompatibleApps
import sdk.sahha.android.domain.manager.IdManager
import sdk.sahha.android.domain.manager.PostChunkManager
import sdk.sahha.android.domain.mapper.HealthConnectConstantsMapper
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.health_connect.HealthConnectChangeToken
import sdk.sahha.android.domain.model.health_connect.HealthConnectQuery
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass

private val TAG = "HealthConnectRepoImpl"

internal class HealthConnectRepoImpl @Inject constructor(
    private val context: Context,
    @DefaultScope private val defaultScope: CoroutineScope,
    @IoScope private val ioScope: CoroutineScope,
    private val chunkManager: PostChunkManager,
    private val configRepo: SahhaConfigRepo,
    private val authRepo: AuthRepo,
    private val sensorRepo: SensorRepo,
    private val workManager: WorkManager,
    private val api: SahhaApi,
    private val client: HealthConnectClient?,
    private val sahhaErrorLogger: SahhaErrorLogger,
    private val sahhaTimeManager: SahhaTimeManager,
    private val healthConnectConfigDao: HealthConnectConfigDao,
    private val movementDao: MovementDao,
    private val mapper: HealthConnectConstantsMapper,
    private val idManager: IdManager,
) : HealthConnectRepo {
    override val permissions =
        setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(RestingHeartRateRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(BloodPressureRecord::class),
            HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        )
    override val successfulQueryTimestamps =
        hashMapOf<String, ZonedDateTime>()

    private var hasMore: List<Boolean> = listOf()
    override var shouldLoop: Boolean = false

    override fun resetHasMore() {
        hasMore = emptyList()
    }

    private fun getDataClassFromPermission(
        permission: String
    ): KClass<out Record>? {
        return when (permission) {
            HealthPermission.getReadPermission(HeartRateRecord::class) -> HeartRateRecord::class
            HealthPermission.getReadPermission(RestingHeartRateRecord::class) -> RestingHeartRateRecord::class
            HealthPermission.getReadPermission(StepsRecord::class) -> StepsRecord::class
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

    override fun startDevicePostWorker(
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        defaultScope.launch {
            val config = configRepo.getConfig()
            if (config.sensorArray.contains(SahhaSensor.device_lock.ordinal)) {
                sensorRepo.startDevicePostWorker(
                    Constants.FIFTEEN_MINUTES,
                    Constants.DEVICE_POST_WORKER_TAG
                )
            }

            callback?.invoke(null, true)
        }
    }

    private fun tryUnregisterExistingReceiver(
        receiver: BroadcastReceiver
    ) {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            Log.w(TAG, e.message ?: "Could not unregister receiver or listener", e)
        }
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

    private suspend fun getStepResponse(stepData: List<SahhaDataLog>): Response<ResponseBody> {
        val token = authRepo.getToken() ?: ""
        return api.postStepDataLog(
            TokenBearer(token),
            stepData
        )
    }

    private suspend fun getSleepSessionResponse(sleepSessionData: List<SahhaDataLog>): Response<ResponseBody> {
        val token = authRepo.getToken() ?: ""
        return api.postSleepDataRange(
            TokenBearer(token),
            sleepSessionData
        )
    }

    private suspend fun getHeartRateDataResponse(heartRateData: List<SahhaDataLog>): Response<ResponseBody> {
        val token = authRepo.getToken() ?: ""
        return api.postHeartRateData(
            TokenBearer(token),
            heartRateData
        )
    }

    private suspend fun getBloodGlucoseResponse(bloodGlucoseData: List<SahhaDataLog>): Response<ResponseBody> {
        val token = authRepo.getToken() ?: ""
        return api.postBloodGlucoseData(
            TokenBearer(token),
            bloodGlucoseData
        )
    }

    private suspend fun getBloodPressureResponse(bloodPressureData: List<SahhaDataLog>): Response<ResponseBody> {
        val token = authRepo.getToken() ?: ""
        return api.postBloodPressureData(
            TokenBearer(token),
            bloodPressureData
        )
    }

    override suspend fun postHeartRateVariabilityRmssdData(
        heartRateVariabilityRmssdData: List<HeartRateVariabilityRmssdRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        val getResponse: suspend (List<HeartRateVariabilityRmssdRecord>) -> Response<ResponseBody> =
            { chunk ->
                val heartRateVariabilityRmssd = chunk.map { it.toSahhaDataLogDto() }
                getHeartRateDataResponse(heartRateVariabilityRmssd)
            }

        postData(
            heartRateVariabilityRmssdData,
            Constants.DEFAULT_POST_LIMIT,
            getResponse,
            { chunk ->
                val last = chunk.last()
                saveLastSuccessfulQuery(
                    HeartRateVariabilityRmssdRecord::class,
                    last.time.atZone(last.zoneOffset)
                )
            },
            callback
        )
    }

    override suspend fun postBloodGlucoseData(
        bloodGlucoseData: List<BloodGlucoseRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        val getResponse: suspend (List<BloodGlucoseRecord>) -> Response<ResponseBody> = { chunk ->
            val bloodGlucose = chunk.map { it.toSahhaDataLogDto() }
            getBloodGlucoseResponse(bloodGlucose)
        }

        postData(
            bloodGlucoseData,
            Constants.DEFAULT_POST_LIMIT,
            getResponse,
            { chunk ->
                val last = chunk.last()
                saveLastSuccessfulQuery(
                    BloodGlucoseRecord::class,
                    last.time.atZone(last.zoneOffset)
                )
            },
            callback
        )
    }

    override suspend fun postBloodPressureData(
        bloodPressureData: List<BloodPressureRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        val getSystolicResponse: suspend (List<BloodPressureRecord>) -> Response<ResponseBody> =
            { chunk ->
                val systolic = chunk.map { it.toBloodPressureSystolic() }
                getBloodPressureResponse(systolic)
            }

        val getDiastolicResponse: suspend (List<BloodPressureRecord>) -> Response<ResponseBody> =
            { chunk ->
                val diastolic = chunk.map { it.toBloodPressureDiastolic() }
                getBloodPressureResponse(diastolic)
            }

        val errors = mutableListOf<String>()
        val successes = mutableListOf<Boolean>()
        suspendCoroutine { cont ->
            ioScope.launch {
                postData(
                    bloodPressureData,
                    Constants.DEFAULT_POST_LIMIT,
                    getSystolicResponse,
                    {},
                ) { error, success ->
                    error?.also { errors.add(it) }
                    successes.add(success)
                    cont.resume(Unit)
                }
            }
        }

        suspendCoroutine { cont ->
            ioScope.launch {
                postData(
                    bloodPressureData,
                    Constants.DEFAULT_POST_LIMIT,
                    getDiastolicResponse,
                    { chunk ->
                        val last = chunk.last()
                        saveLastSuccessfulQuery(
                            BloodPressureRecord::class,
                            last.time.atZone(last.zoneOffset)
                        )
                    },
                ) { error, success ->
                    error?.also { errors.add(it) }
                    successes.add(success)
                    cont.resume(Unit)
                }
            }
        }

        callback?.invoke(errors?.toString(), successes.all { it })

        if (!successes.all { it })
            sahhaErrorLogger.application(
                errors.toString(),
                TAG,
                "postBloodPressureData",
                bloodPressureData.map {
                    it.toBloodPressureSystolic()
                    it.toBloodPressureDiastolic()
                }.toString()
            )
    }

    override suspend fun postSleepSessionData(
        sleepSessionData: List<SleepSessionRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        val sleepStages = mutableListOf<SahhaDataLog>()
        val sleepSessions = sleepSessionData.map { it.toSahhaDataLogDto() }
        sleepSessionData.forEach { session ->
            session.stages.forEach { s ->
                val durationInMinutes =
                    ((s.endTime.toEpochMilli() - s.startTime.toEpochMilli()) / 1000 / 60).toDouble()
                sleepStages.add(
                    SahhaDataLog(
                        id = UUID.nameUUIDFromBytes(
                            (session.metadata.id + s.startTime + s.endTime)
                                .toByteArray()
                        ).toString(),
                        parentId = session.metadata.id,
                        logType = Constants.DataLogs.SLEEP,
                        value = durationInMinutes,
                        unit = Constants.DataUnits.MINUTE,
                        source = session.metadata.dataOrigin.packageName,
                        dataType = (mapper.sleepStages(s.stage)
                            ?: Constants.SLEEP_STAGE_UNKNOWN),
                        startDateTime = sahhaTimeManager.instantToIsoTime(
                            s.startTime, session.startZoneOffset
                        ),
                        endDateTime = sahhaTimeManager.instantToIsoTime(
                            s.endTime, session.endZoneOffset
                        ),
                        recordingMethod = mapper.recordingMethod(session.metadata.recordingMethod),
                        deviceId = idManager.getDeviceId(),
                        deviceType = mapper.devices(session.metadata.device?.type),
                    )
                )
            }
        }
        val sessionsAndStages = sleepSessions + sleepStages

        val getResponse: suspend (List<SahhaDataLog>) -> Response<ResponseBody> = { chunk ->
            getSleepSessionResponse(chunk)
        }

        postData(
            sessionsAndStages,
            Constants.DEFAULT_POST_LIMIT,
            getResponse,
            { chunk ->
                val last = chunk.last()
                saveLastSuccessfulQuery(
                    SleepSessionRecord::class,
                    sahhaTimeManager.ISOToZonedDateTime(last.endDateTime)
                )
            },
            callback
        )
    }

    override suspend fun postHeartRateData(
        heartRateData: List<HeartRateRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        val samplesList = mutableListOf<SahhaDataLog>()
        heartRateData.forEach { record ->
            record.samples.forEach { sample ->
                samplesList.add(
                    SahhaDataLog(
                        id = UUID.nameUUIDFromBytes(
                            (record.metadata.id + sample.time)
                                .toByteArray()
                        ).toString(),
                        parentId = record.metadata.id,
                        logType = Constants.DataLogs.HEART,
                        dataType = SahhaSensor.heart_rate.name,
                        value = sample.beatsPerMinute.toDouble(),
                        unit = Constants.DataUnits.BEAT_PER_MIN,
                        source = record.metadata.dataOrigin.packageName,
                        startDateTime = sahhaTimeManager.instantToIsoTime(
                            sample.time, record.startZoneOffset
                        ),
                        endDateTime = sahhaTimeManager.instantToIsoTime(
                            sample.time, record.endZoneOffset
                        ),
                        recordingMethod = mapper.recordingMethod(record.metadata.recordingMethod),
                        deviceId = idManager.getDeviceId(),
                        deviceType = mapper.devices(record.metadata.device?.type),
                    )
                )
            }
        }

        val getResponse: suspend (List<SahhaDataLog>) -> Response<ResponseBody> = { chunk ->
            getHeartRateDataResponse(chunk)
        }

        postData(
            samplesList,
            Constants.DEFAULT_POST_LIMIT,
            getResponse,
            { chunk ->
                val last = chunk.last()
                saveLastSuccessfulQuery(
                    HeartRateRecord::class,
                    sahhaTimeManager.ISOToZonedDateTime(last.endDateTime)
                )
            },
            callback
        )
    }

    override suspend fun postRestingHeartRateData(
        restingHeartRateData: List<RestingHeartRateRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        val getResponse: suspend (List<RestingHeartRateRecord>) -> Response<ResponseBody> =
            { chunk ->
                val restingRates = chunk.map { it.toSahhaLogDto() }
                getHeartRateDataResponse(restingRates)
            }

        postData(
            restingHeartRateData,
            Constants.DEFAULT_POST_LIMIT,
            getResponse,
            { chunk ->
                val last = chunk.last()
                saveLastSuccessfulQuery(
                    RestingHeartRateRecord::class,
                    last.time.atZone(last.zoneOffset)
                )
            },
            callback
        )
    }

    override suspend fun postStepData(
        stepData: List<StepsHealthConnect>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        val getResponse: suspend (List<StepsHealthConnect>) -> Response<ResponseBody> = { chunk ->
            val steps = chunk.map { it.toSahhaDataLogAsChildLog() }
            getStepResponse(steps)
        }

        postData(
            stepData,
            Constants.DEFAULT_POST_LIMIT,
            getResponse,
            { chunk ->
                val last = chunk.last()
                saveLastSuccessfulQuery(
                    StepsRecord::class,
                    sahhaTimeManager.ISOToZonedDateTime(last.endDateTime)
                )
            },
            callback
        )
    }

    override suspend fun postOxygenSaturation(
        oxygenSaturationData: List<OxygenSaturationRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        val getResponse: suspend (List<OxygenSaturationRecord>) -> Response<ResponseBody> =
            { chunk ->
                val token = authRepo.getToken() ?: ""
                val oxygenSaturation = chunk.map { it.toSahhaDataLogDto() }
                api.postOxygenSaturation(
                    TokenBearer(token),
                    oxygenSaturation
                )
            }

        postData(
            oxygenSaturationData,
            Constants.DEFAULT_POST_LIMIT,
            getResponse,
            { chunk ->
                val last = chunk.last()
                saveLastSuccessfulQuery(
                    OxygenSaturationRecord::class,
                    last.time.atZone(last.zoneOffset)
                )
            },
            callback
        )
    }

    override suspend fun postActiveEnergyBurned(
        activeCalBurnedData: List<ActiveCaloriesBurnedRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        val getResponse: suspend (List<ActiveCaloriesBurnedRecord>) -> Response<ResponseBody> =
            { chunk ->
                val token = authRepo.getToken() ?: ""
                val totalCaloriesBurned = chunk.map { it.toSahhaDataLogDto() }
                api.postActiveCaloriesBurned(
                    TokenBearer(token),
                    totalCaloriesBurned
                )
            }

        postData(
            activeCalBurnedData,
            Constants.DEFAULT_POST_LIMIT,
            getResponse,
            { chunk ->
                val last = chunk.last()
                saveLastSuccessfulQuery(
                    ActiveCaloriesBurnedRecord::class,
                    last.endTime.atZone(last.endZoneOffset)
                )
            },
            callback
        )
    }

    override suspend fun postTotalEnergyBurned(
        totalCaloriesBurnedData: List<TotalCaloriesBurnedRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        val getResponse: suspend (List<TotalCaloriesBurnedRecord>) -> Response<ResponseBody> =
            { chunk ->
                val token = authRepo.getToken() ?: ""
                val totalCaloriesBurned = chunk.map { it.toSahhaDataLogDto() }
                api.postTotalCaloriesBurned(
                    TokenBearer(token),
                    totalCaloriesBurned
                )
            }

        postData(
            totalCaloriesBurnedData,
            Constants.DEFAULT_POST_LIMIT,
            getResponse,
            { chunk ->
                val last = chunk.last()
                saveLastSuccessfulQuery(
                    TotalCaloriesBurnedRecord::class,
                    last.endTime.atZone(last.endZoneOffset)
                )
            },
            callback
        )
    }

    override suspend fun postVo2MaxData(
        vo2MaxData: List<Vo2MaxRecord>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        val getResponse: suspend (List<Vo2MaxRecord>) -> Response<ResponseBody> =
            { chunk ->
                val token = authRepo.getToken() ?: ""
                val vo2Max = chunk.map { it.toSahhaDataLogDto() }
                api.postVo2Max(
                    TokenBearer(token),
                    vo2Max
                )
            }

        postData(
            vo2MaxData,
            Constants.DEFAULT_POST_LIMIT,
            getResponse,
            { chunk ->
                val last = chunk.last()
                saveLastSuccessfulQuery(
                    Vo2MaxRecord::class,
                    last.time.atZone(last.zoneOffset)
                )
            },
            callback
        )
    }

    override suspend fun <T> postData(
        data: List<T>,
        chunkLimit: Int,
        getResponse: suspend (List<T>) -> Response<ResponseBody>,
        updateLastQueried: suspend (List<T>) -> Unit,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        try {
            if (data.isEmpty()) {
                callback?.invoke(null, true)
                return
            }

            chunkManager.postAllChunks(
                data,
                chunkLimit,
                { chunk ->
                    sendChunk(chunk, getResponse, updateLastQueried)
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
        updateLastQueried: suspend (List<T>) -> Unit,
    ): Boolean {
        return suspendCancellableCoroutine { cont ->
            ioScope.launch {
                try {
                    val response = getResponse(chunk)
                    Log.d(TAG, "Content length: ${response.raw().request.body?.contentLength()}")

                    handleResponse(response, { getResponse(chunk) }, null) {
                        // When successful
                        updateLastQueried(chunk)
                        if (cont.isActive) cont.resume(true)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.message, e)
                    if (cont.isActive) cont.resume(false)
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
            val code = response.code()

            if (ResponseCode.accountRemoved(code)) {
                Log.w(TAG, "Account does not exist, stopping all tasks")
                Sahha.sim.auth.deauthenticate { error, _ -> error?.also { Log.w(TAG, it) } }
                Sahha.sim.sensor.stopAllBackgroundTasks(context)
                Sahha.sim.sensor.killMainService(context)
                return
            }

            if (ResponseCode.isUnauthorized(code)) {
                if (Session.tokenRefreshAttempted) return

                callback?.invoke(SahhaErrors.attemptingTokenRefresh, false)
                SahhaResponseHandler.checkTokenExpired(code) {
                    val retryResponse = retryLogic()
                    handleResponse(
                        retryResponse,
                        retryLogic,
                        callback,
                        successfulLogic
                    )
                    Session.tokenRefreshAttempted = true
                }
                return
            }

            if (ResponseCode.isSuccessful(code)) {
                successfulLogic?.invoke()
                callback?.also {
                    it(null, true)
                }
                return
            }

            callback?.also {
                it(
                    "${code}: ${response.message()}",
                    false
                )
            }

            sahhaErrorLogger.apiFromJsonArray(response)
        } catch (e: Exception) {
            callback?.also {
                it(e.message, false)
            }

            sahhaErrorLogger.application(
                e.message ?: SahhaErrors.somethingWentWrong,
                TAG,
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

    override suspend fun <T : Record> getLastSuccessfulQuery(recordType: KClass<T>): ZonedDateTime? {
        val permissionString = HealthPermission.getReadPermission(recordType)
        val query = healthConnectConfigDao.getQueryOf(permissionString)
        val instant = query?.let { q ->
            Instant.ofEpochMilli(q.lastSuccessfulTimeStampEpochMillis)
        } ?: return null

        return ZonedDateTime.ofInstant(instant, sahhaTimeManager.zoneOffset)
    }

    override suspend fun <T : Record> saveLastSuccessfulQuery(
        recordType: KClass<T>,
        timeStamp: ZonedDateTime
    ) {
        val permissionString = HealthPermission.getReadPermission(recordType)
        val epochMillis = timeStamp.toInstant().toEpochMilli()

        healthConnectConfigDao.saveQuery(
            HealthConnectQuery(
                permissionString,
                epochMillis
            )
        )
    }

    override suspend fun saveCustomSuccessfulQuery(
        customId: String,
        timeStamp: ZonedDateTime
    ) {
        val epochMillis = timeStamp.toInstant().toEpochMilli()

        healthConnectConfigDao.saveQuery(
            HealthConnectQuery(customId, epochMillis)
        )
    }

    override suspend fun getLastCustomQuery(
        customId: String
    ): HealthConnectQuery? {
        return healthConnectConfigDao.getQueryOf(customId)
    }

    override suspend fun clearQueries(queries: List<HealthConnectQuery>) {
        healthConnectConfigDao.clearQueries(queries)
    }

    override suspend fun clearAllQueries() {
        healthConnectConfigDao.clearAllQueries()
    }

    override suspend fun clearAllChangeTokens() {
        healthConnectConfigDao.clearAllChangeTokens()
    }

    private suspend fun getChangeToken(recordType: String): String? {
        return healthConnectConfigDao.getChangeToken(recordType)?.token
    }

    override suspend fun <T : Record> getRecords(
        recordType: KClass<T>,
        timeRangeFilter: TimeRangeFilter
    ): List<T> {
        try {
            val recordTypeString = HealthPermission.getReadPermission(recordType)
            val pageTokenId = recordTypeString + Constants.PAGE_TOKEN_SUFFIX
            val pageToken = getChangeToken(pageTokenId)
            val response = client?.readRecords(
                ReadRecordsRequest(
                    recordType = recordType,
                    timeRangeFilter = timeRangeFilter,
                    pageToken = pageToken
                )
            )
            try {
                saveChangeToken(
                    HealthConnectChangeToken(
                        pageTokenId,
                        response?.pageToken
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, e.message ?: "Something went wrong storing next page token")
            }

            return response?.records ?: emptyList()
        } catch (e: Exception) {
            Log.w(TAG, e.message ?: "Could not query Health Connect data")
            return emptyList()
        }
    }

    override suspend fun <T : Record> getChangedRecords(
        recordType: KClass<T>,
        token: String?
    ): List<Record>? {
        client ?: return emptyList()
        val recordTypeString = HealthPermission.getReadPermission(recordType)
        val pageToken = getChangeToken(recordType = recordTypeString + Constants.PAGE_TOKEN_SUFFIX)
        val pageTokenExists = !pageToken.isNullOrEmpty()
        if (pageTokenExists) return null

        var t = token
        val noTokenFound = t == null
        if (noTokenFound) {
            t = client.getChangesToken(
                ChangesTokenRequest(
                    recordTypes = setOf(recordType),
                )
            )
            storeNextChangesToken(recordType, t)
            return null
        }

        // t is null checked
        val changed = mutableListOf<Record>()
        var response: ChangesResponse?
        do {
            try {
                response = client.getChanges(t!!)
                if (response.changesTokenExpired) {
                    val newToken = client.getChangesToken(
                        ChangesTokenRequest(
                            recordTypes = setOf(recordType),
                        )
                    )
                    response = client.getChanges(newToken)
                }
                response.changes.forEach {
                    when (it) {
                        is UpsertionChange -> {
                            changed.add(it.record)
                        }
                    }
                }
                t = response.nextChangesToken
            } catch (e: Exception) {
                Log.d(TAG, e.message, e)
                shouldLoop = false
                return emptyList()
            }
        } while (response?.hasMore == true)
        response?.also {
            hasMore += it.hasMore
            shouldLoop = hasMore.contains(true)
        }

        storeNextChangesToken(recordType, t!!)
        return changed
    }

    private suspend fun <T : Record> storeNextChangesToken(recordType: KClass<T>, token: String) {
        val recordTypeString = HealthPermission.getReadPermission(recordType)
        healthConnectConfigDao.saveChangeToken(
            HealthConnectChangeToken(
                recordTypeString,
                token
            )
        )
    }

    override suspend fun <T : Record> getExistingChangesToken(recordType: KClass<T>): String? {
        val recordTypeString = HealthPermission.getReadPermission(recordType)
        return healthConnectConfigDao.getChangeToken(recordTypeString)?.token
    }

    override suspend fun <T : Record> getNewRecords(dataType: KClass<T>): List<T>? {
        return if (isFirstQuery(dataType)) runInitialQuery(dataType)
        else runAfterQuery(dataType)
    }

    override suspend fun <T : Record> getCurrentDayRecords(dataType: KClass<T>): List<T>? {
        return if (isFirstQuery(dataType)) runBeforeQueryAndStoreMidnight(dataType)
        else runAfterQueryFromMidnight(dataType)
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

    override suspend fun saveChangeToken(changeToken: HealthConnectChangeToken) {
        healthConnectConfigDao.saveChangeToken(changeToken = changeToken)
    }

    override suspend fun clearStepsBeforeHc(dateTime: LocalDateTime) {
        val iso = sahhaTimeManager.localDateTimeToISO(dateTime)
        movementDao.clearStepsBeforeHc(iso)
    }

    private suspend fun <T : Record> isFirstQuery(dataType: KClass<T>): Boolean {
        val recordTypeString = HealthPermission.getReadPermission(dataType)
        val pageTokenId = recordTypeString + Constants.PAGE_TOKEN_SUFFIX
        val pageTokenExists = !getChangeToken(pageTokenId).isNullOrEmpty()
        if (pageTokenExists) return true

        return getLastSuccessfulQuery(dataType)?.let { false } ?: true
    }

    private suspend fun <T : Record> runInitialQuery(dataType: KClass<T>): List<T>? {
        val now = ZonedDateTime.now()
        val records = getRecords(
            dataType,
            TimeRangeFilter.before(now.toInstant())
        )
        if (records.isEmpty()) return null

        successfulQueryTimestamps[HealthPermission.getReadPermission(dataType)] = now
        hasMore += true
        shouldLoop = hasMore.contains(true)
        return records
    }

    private suspend fun <T : Record> runBeforeQueryAndStoreMidnight(dataType: KClass<T>): List<T>? {
        val now = ZonedDateTime.now()
        val localMidnight = LocalDateTime.of(
            LocalDate.now(), LocalTime.MIDNIGHT
        )
        val currentMidnight = ZonedDateTime.of(localMidnight, sahhaTimeManager.zoneOffset)
        val records = getRecords(
            dataType,
            TimeRangeFilter.before(now.toLocalDateTime())
        )
        if (records.isEmpty()) return null

        successfulQueryTimestamps[HealthPermission.getReadPermission(dataType)] =
            currentMidnight
        hasMore += true
        shouldLoop = hasMore.contains(true)
        return records
    }

    private suspend fun <T : Record> runAfterQuery(dataType: KClass<T>): List<T>? {
        val lastQueryTimestamp = getLastSuccessfulQuery(dataType)

        return lastQueryTimestamp?.let { timestamp ->
            val records = getRecords(
                dataType, TimeRangeFilter.after(timestamp.toInstant())
            )
            if (records.isEmpty()) return@let null

            successfulQueryTimestamps[HealthPermission.getReadPermission(dataType)] =
                ZonedDateTime.now()
            return@let records
        }
    }

    private suspend fun <T : Record> runAfterQueryFromMidnight(dataType: KClass<T>): List<T>? {
        val timestamp = getLastQueryTimeStampForSteps(dataType)
        val local = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT)
        val now = ZonedDateTime.of(local, sahhaTimeManager.zoneOffset)

        val records = getRecords(
            dataType,
            TimeRangeFilter.after(timestamp)
        )
        if (records.isEmpty()) return null

        successfulQueryTimestamps[HealthPermission.getReadPermission(dataType)] = now
        return records
    }

    private suspend fun <T : Record> getLastQueryTimeStampForSteps(dataType: KClass<T>): LocalDateTime {
        val timestampDate = getLastSuccessfulQuery(dataType)
            ?.toLocalDate()

        return LocalDateTime.of(timestampDate, LocalTime.MIDNIGHT)
    }
}