package sdk.sahha.android.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.Constants.DEVICE_POST_WORKER_TAG
import sdk.sahha.android.common.Constants.SLEEP_POST_WORKER_TAG
import sdk.sahha.android.common.Constants.STEP_POST_WORKER_TAG
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaReceiversAndListeners
import sdk.sahha.android.common.SahhaResponseHandler
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.TokenBearer
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.data.local.dao.SleepDao
import sdk.sahha.android.data.mapper.toSahhaDataLogAsChildLog
import sdk.sahha.android.data.mapper.toSahhaDataLogDto
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.di.DefaultScope
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.manager.PostChunkManager
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.model.config.toSetOfSensors
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.StepSession
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.DeviceUsageRepo
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.framework.worker.BackgroundTaskRestarterWorker
import sdk.sahha.android.framework.worker.HealthConnectQueryWorker
import sdk.sahha.android.framework.worker.SleepCollectionWorker
import sdk.sahha.android.framework.worker.post.BatchedDataPostWorker
import sdk.sahha.android.framework.worker.post.DevicePostWorker
import sdk.sahha.android.framework.worker.post.SleepPostWorker
import sdk.sahha.android.framework.worker.post.StepPostWorker
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaConverterUtility
import sdk.sahha.android.source.SahhaSensor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume

private const val tag = "SensorRepoImpl"

@SuppressLint("NewApi")
internal class SensorRepoImpl @Inject constructor(
    private val context: Context,
    @DefaultScope private val defaultScope: CoroutineScope,
    @IoScope private val ioScope: CoroutineScope,
    private val deviceUsageRepo: DeviceUsageRepo,
    private val sleepDao: SleepDao,
    private val movementDao: MovementDao,
    private val authRepo: AuthRepo,
    private val sahhaConfigRepo: SahhaConfigRepo,
    private val sahhaErrorLogger: SahhaErrorLogger,
    private val mutex: Mutex,
    private val api: SahhaApi,
    private val chunkManager: PostChunkManager,
    private val permissionManager: PermissionManager,
    private val timeManager: SahhaTimeManager,
) : SensorRepo {
    private val workManager by lazy { WorkManager.getInstance(context) }
    private val sensorToWorkerAction = mapOf(
        SahhaSensor.sleep to Pair(SLEEP_POST_WORKER_TAG, ::startSleepPostWorker),
        SahhaSensor.device_lock to Pair(DEVICE_POST_WORKER_TAG, ::startDevicePostWorker),
        SahhaSensor.steps to Pair(STEP_POST_WORKER_TAG, ::startStepPostWorker)
    )

    override suspend fun startStepDetectorAsync(
        context: Context,
        movementDao: MovementDao,
    ) {
        val sensorManager = Sahha.di.sensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        sensor?.also {
            sensorManager.registerListener(
                SahhaReceiversAndListeners.stepDetector, it, SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override suspend fun startStepCounterAsync(
        context: Context,
        movementDao: MovementDao,
    ) {
        val sensorManager = Sahha.di.sensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        sensor?.also {
            sensorManager.registerListener(
                SahhaReceiversAndListeners.stepCounter,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun startSleepWorker(repeatIntervalMinutes: Long, workerTag: String) {
        val checkedIntervalMinutes = getCheckedIntervalMinutes(repeatIntervalMinutes)
        val workRequest: PeriodicWorkRequest =
            getSleepWorkRequest(checkedIntervalMinutes, workerTag)
        startWorkManager(workRequest, workerTag, ExistingPeriodicWorkPolicy.REPLACE)
    }

    override fun checkAndStartWorker(
        config: SahhaConfiguration,
        sensorId: Int,
        startWorker: () -> Unit
    ) {
        if (config.sensorArray.contains(sensorId)) startWorker()
    }

    override fun stopWorkerByTag(workerTag: String) {
        workManager.cancelAllWorkByTag(workerTag)
    }

    override fun stopAllWorkers() {
        workManager.cancelAllWork()
    }

    override suspend fun getSensorData(
        sensor: SahhaSensor,
        callback: suspend (error: String?, successful: String?) -> Unit
    ) {
        try {
            when (sensor) {
                SahhaSensor.device_lock -> {
                    val deviceSummary = getDeviceDataSummary()
                    if (deviceSummary.isNotEmpty()) {
                        callback(null, deviceSummary)
                        return
                    }
                }

                SahhaSensor.sleep -> {
                    val sleepSummary = getSleepDataSummary()
                    if (sleepSummary.isNotEmpty()) {
                        callback(null, sleepSummary)
                        return
                    }
                }

                SahhaSensor.steps -> {
                    val stepSummary = getStepDataSummary()
                    if (stepSummary.isNotEmpty()) {
                        callback(null, stepSummary)
                        return
                    }
                }

                else -> return
            }
            callback("No data found", null)
        } catch (e: Exception) {
            callback("Error: " + e.message, null)
        }
    }

    private suspend fun getDeviceDataSummary(): String {
        var dataSummary = ""
        deviceUsageRepo.getUsages().forEach {
            dataSummary += "Locked: ${it.isLocked}\nScreen on: ${it.isScreenOn}\nAt: ${it.createdAt}\n\n"
        }
        return dataSummary
    }

    private suspend fun getStepDataSummary(): String {
        var dataSummary = ""
        movementDao.getAllStepData().forEach {
            if (it.source == Constants.STEP_DETECTOR_DATA_SOURCE)
                dataSummary += "${it.count} step\nAt: ${it.detectedAt}\n\n"
            if (it.source == Constants.STEP_COUNTER_DATA_SOURCE)
                dataSummary += "${it.count} total steps since last phone boot\nAt: ${it.detectedAt}\n\n"
        }
        return dataSummary
    }

    private suspend fun getSleepDataSummary(): String {
        var dataSummary = ""
        sleepDao.getSleepDto().forEach {
            dataSummary += "Slept: ${it.durationInMinutes} minutes\nFrom: ${it.startDateTime}\nTo: ${it.endDateTime}\n\n"
        }
        return dataSummary
    }

    override fun startSleepPostWorker(repeatIntervalMinutes: Long, workerTag: String) {
        val checkedIntervalMinutes = getCheckedIntervalMinutes(repeatIntervalMinutes)
        val workRequest = getSleepPostWorkRequest(checkedIntervalMinutes, workerTag)
        startWorkManager(workRequest, workerTag)
    }

    override fun startDevicePostWorker(repeatIntervalMinutes: Long, workerTag: String) {
        val checkedIntervalMinutes = getCheckedIntervalMinutes(repeatIntervalMinutes)
        val workRequest = getDevicePostWorkRequest(checkedIntervalMinutes, workerTag)
        startWorkManager(workRequest, workerTag)
    }

    override fun startStepPostWorker(repeatIntervalMinutes: Long, workerTag: String) {
        val checkedIntervalMinutes = getCheckedIntervalMinutes(repeatIntervalMinutes)
        val workRequest = getStepPostWorkRequest(checkedIntervalMinutes, workerTag)
        startWorkManager(workRequest, workerTag)
    }

    override fun startBatchedDataPostWorker(repeatIntervalMinutes: Long, workerTag: String) {
        val checkedIntervalMinutes = getCheckedIntervalMinutes(repeatIntervalMinutes)
        val workRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<BatchedDataPostWorker>(
                checkedIntervalMinutes,
                TimeUnit.MINUTES
            )
                .addTag(workerTag)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()
        startWorkManager(workRequest, workerTag, ExistingPeriodicWorkPolicy.REPLACE)
    }

    override fun startOneTimeBatchedDataPostWorker(workerTag: String) {
        val workRequest: OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<BatchedDataPostWorker>()
                .addTag(workerTag)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()
        startOneTimeWork(workRequest, workerTag)
    }

    override fun startHealthConnectQueryWorker(repeatIntervalMinutes: Long, workerTag: String) {
        val workRequest = PeriodicWorkRequestBuilder<HealthConnectQueryWorker>(
            repeatIntervalMinutes,
            TimeUnit.MINUTES
        )
            .addTag(workerTag)
            .build()
        startWorkManager(workRequest, workerTag, policy = ExistingPeriodicWorkPolicy.REPLACE)
    }

    override fun startBackgroundTaskRestarterWorker(
        repeatIntervalMinutes: Long,
        workerTag: String
    ) {
        val workRequest = PeriodicWorkRequestBuilder<BackgroundTaskRestarterWorker>(
            repeatIntervalMinutes,
            TimeUnit.MINUTES
        )
            .addTag(workerTag)
            .build()
        startWorkManager(workRequest, workerTag)
    }

    // Force default minimum value of 15 minutes
    private fun getCheckedIntervalMinutes(interval: Long): Long {
        return if (interval < 15) 15 else interval
    }

    private fun getSleepPostWorkRequest(
        repeatIntervalMinutes: Long,
        workerTag: String
    ): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<SleepPostWorker>(
            repeatIntervalMinutes,
            TimeUnit.MINUTES
        )
            .addTag(workerTag)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()
    }

    private fun getStepPostWorkRequest(
        repeatIntervalMinutes: Long,
        workerTag: String
    ): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<StepPostWorker>(
            repeatIntervalMinutes,
            TimeUnit.MINUTES
        )
            .addTag(workerTag)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()
    }

    private fun getDevicePostWorkRequest(
        repeatIntervalMinutes: Long,
        workerTag: String
    ): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<DevicePostWorker>(
            repeatIntervalMinutes,
            TimeUnit.MINUTES
        )
            .addTag(workerTag)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
    }

    private fun getSleepWorkRequest(
        repeatIntervalMinutes: Long,
        workerTag: String
    ): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<SleepCollectionWorker>(
            repeatIntervalMinutes,
            TimeUnit.MINUTES
        )
            .addTag(workerTag)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()
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

    private fun startOneTimeWork(
        workRequest: OneTimeWorkRequest,
        workerTag: String,
        policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP
    ) {
        workManager.enqueueUniqueWork(
            workerTag,
            policy,
            workRequest
        )
    }

    private fun getFilteredStepData(stepData: List<StepData>): List<StepData> {
        return if (stepData.count() > 1000) {
            stepData.subList(0, 1000)
        } else stepData
    }

    override suspend fun postStepData(
        stepData: List<StepData>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        val getResponse: suspend (List<StepData>) -> Response<ResponseBody> = { chunk ->
            val sahhaDataLogs = getFilteredStepData(chunk).map { it.toSahhaDataLogAsChildLog() }
            getStepResponse(sahhaDataLogs)
        }
        postData(
            stepData,
            SahhaSensor.steps,
            Constants.STEP_POST_LIMIT,
            getResponse,
            movementDao::clearStepData,
            callback
        )
    }

    override suspend fun postStepSessions(
        stepSessions: List<StepSession>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        val getResponse: suspend (List<StepSession>) -> Response<ResponseBody> = { chunk ->
            val sahhaDataLogs = chunk.map { it.toSahhaDataLogAsChildLog() }
            getStepResponse(sahhaDataLogs)
        }
        postData(
            stepSessions,
            SahhaSensor.steps,
            Constants.STEP_SESSION_POST_LIMIT,
            getResponse,
            this::clearStepSessions,
            callback
        )
    }

    override suspend fun postSleepData(
        sleepData: List<SleepDto>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        postData(
            sleepData,
            SahhaSensor.sleep,
            Constants.SLEEP_POST_LIMIT,
            this::getSleepResponse,
            sleepDao::clearSleepDto,
            callback
        )
    }

    override suspend fun postPhoneScreenLockData(
        phoneLockData: List<PhoneUsage>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        postData(
            phoneLockData,
            SahhaSensor.device_lock,
            Constants.DEVICE_LOCK_POST_LIMIT,
            this::getPhoneScreenLockResponse,
            deviceUsageRepo::clearUsages,
            callback
        )
    }


    override suspend fun <T> postData(
        data: List<T>,
        sensor: SahhaSensor,
        chunkLimit: Int,
        getResponse: suspend (List<T>) -> Response<ResponseBody>,
        clearData: suspend (List<T>) -> Unit,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        try {
            if (data.isEmpty()) {
                callback?.invoke(SahhaErrors.localDataIsEmpty(sensor), false)
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
            callback?.invoke(e.message, false)
            handleException(e, "postData", data.toString(), callback)
        }
    }

    override suspend fun <T> sendChunk(
        chunk: List<T>,
        getResponse: suspend (List<T>) -> Response<ResponseBody>,
        clearData: suspend (List<T>) -> Unit,
    ): Boolean {
        return suspendCancellableCoroutine { cont ->
            ioScope.launch {
                try {
                    val response = getResponse(chunk)
                    Log.d(tag, "Content length: ${response.raw().request.body?.contentLength()}")

                    handleResponse(response, { getResponse(chunk) }, null) {
                        clearData(chunk)
                        if (cont.isActive) cont.resume(true)
                    }
                } catch (e: Exception) {
                    Log.w(tag, e.message, e)
                    if (cont.isActive) cont.resume(false)
                }
            }
        }
    }

    override suspend fun handleException(
        e: Exception,
        functionName: String,
        data: String,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        callback?.invoke(e.message, false)

        sahhaErrorLogger.application(
            e.message ?: SahhaErrors.somethingWentWrong,
            tag,
            functionName,
            data
        )
    }


    override suspend fun postAllSensorData(
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        try {
            postSensorData(callback)
        } catch (e: Exception) {
            callback?.invoke(e.message, false)
            sahhaErrorLogger.application(
                e.message ?: SahhaErrors.somethingWentWrong,
                tag,
                "postAllSensorData",
                null
            )
        }
    }

    private fun returnFormattedResponse(
        response: Response<ResponseBody>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        if (response.code() == 204) {
            callback?.invoke(null, "{}")
            return
        }

        val reader = response.body()?.charStream()
        val bodyString = reader?.readText()
        val json = JSONObject(bodyString ?: "")
        val jsonString = json.toString(6)
        callback?.invoke(null, jsonString)
    }

    internal suspend fun handleResponse(
        response: Response<ResponseBody>,
        retryLogic: suspend (() -> Response<ResponseBody>),
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?,
        successfulLogic: (suspend () -> Unit)? = null
    ) {
        try {
            val code = response.code()

            if (ResponseCode.accountRemoved(code)) {
                Log.w(tag, "Account does not exist, stopping all tasks")
                Sahha.sim.auth.deauthenticate { error, successful ->
                    error?.also { Log.w(tag, it) }
                    if (successful) Log.w(tag, "Successfully de-authenticated")
                }
                Sahha.sim.sensor.stopAllBackgroundTasks(context)
                Sahha.sim.sensor.killMainService(context)
                return
            }

            if (ResponseCode.isUnauthorized(code)) {
                callback?.invoke(SahhaErrors.attemptingTokenRefresh, false)
                SahhaResponseHandler.checkTokenExpired(code) {
                    val retryResponse = retryLogic()
                    handleResponse(
                        retryResponse,
                        retryLogic,
                        callback,
                        successfulLogic
                    )
                }

                sahhaErrorLogger.application(
                    SahhaErrors.attemptingTokenRefresh,
                    tag,
                    "handleResponse",
                    SahhaConverterUtility.requestBodyToString(
                        response.raw().request.body
                    )
                )
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
                tag,
                "handleResponse",
                e.stackTraceToString()
            )
        }
    }

    private suspend fun postSensorData(
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        var errorSummary = ""
        var successfulResults = mutableListOf<Boolean>()

        val sensors = Sahha.di.configurationDao.getConfig().toSetOfSensors()
            .ifEmpty { SahhaSensor.values().toSet() }
        if (mutex.tryLock()) {
            sensors.map { sensor ->
                val deferredResult = CompletableDeferred<Unit>()
                postSensorDataForType(sensor) { error, successful ->
                    val (updatedErrorSummary, updatedSuccessfulResults) = updateErrorSummaryAndSuccessfulResults(
                        error,
                        successful,
                        errorSummary,
                        successfulResults
                    )
                    errorSummary = updatedErrorSummary
                    successfulResults = updatedSuccessfulResults

                    if (successful) Log.i(tag, "Successfully posted ${sensor.name} data.")
                    else error?.also { Log.i(tag, "Error posting ${sensor.name} data: $it") }

                    deferredResult.complete(Unit)
                }
            }
            mutex.unlock()

            if (successfulResults.contains(false)) {
                callback?.invoke(errorSummary, false)
                return
            }
        } else {
            // Mutex is locked, so a posting is already in progress
            callback?.invoke(SahhaErrors.postingInProgress, false)
        }

        callback?.invoke(null, true)
    }

    private suspend fun postSensorDataForType(
        sensor: SahhaSensor,
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        val deferredResult = CompletableDeferred<Unit>()
        try {
            when (sensor) {
                SahhaSensor.sleep -> {
                    postSleepData(sleepDao.getSleepDto()) { error, successful ->
                        callback(error, successful)
                        deferredResult.complete(Unit)
                    }
                }

                SahhaSensor.device_lock -> {
                    postPhoneScreenLockData(deviceUsageRepo.getUsages()) { error, successful ->
                        callback(error, successful)
                        deferredResult.complete(Unit)
                    }
                }

                SahhaSensor.steps -> {
                    postStepSessions(getAllStepSessions()) { error, successful ->
                        callback(error, successful)
                        deferredResult.complete(Unit)
                    }
                }

                else -> {
                    callback(null, true)
                    deferredResult.complete(Unit)
                }
            }
        } finally {
            deferredResult.await()
        }
    }

    private fun updateErrorSummaryAndSuccessfulResults(
        error: String?,
        successful: Boolean,
        errorSummary: String,
        successfulResults: MutableList<Boolean>
    ): Pair<String, MutableList<Boolean>> {
        var updatedErrorSummary = errorSummary
        error?.also { updatedErrorSummary += "$it\n" }
        rescheduleWorker(SahhaSensor.sleep)
        successfulResults.add(successful)
        return Pair(updatedErrorSummary, successfulResults)
    }


    private fun rescheduleWorker(sensor: Enum<SahhaSensor>) {
        sensorToWorkerAction[sensor]?.let { (workerTag, startWorkerAction) ->
            stopWorkerByTag(workerTag)
            startWorkerAction(Constants.FIFTEEN_MINUTES, workerTag)
        }
    }

    private suspend fun getStepResponse(stepData: List<SahhaDataLog>): Response<ResponseBody> {
        val token = authRepo.getToken() ?: ""
        return api.postStepDataLog(
            TokenBearer(token),
            stepData
        )
    }

    private suspend fun getSleepResponse(sleepData: List<SleepDto>): Response<ResponseBody> {
        val token = authRepo.getToken() ?: ""
        return api.postSleepDataRange(
            TokenBearer(token),
            sleepData.map { it.toSahhaDataLogDto() }
        )
    }

    private suspend fun getPhoneScreenLockResponse(
        phoneLockData: List<PhoneUsage>,
    ): Response<ResponseBody> {
        val token = authRepo.getToken() ?: ""
        return api.postDeviceActivityRange(
            TokenBearer(token),
            phoneLockData.map { it.toSahhaDataLogDto() }
        )
    }

    override suspend fun saveStepSession(stepSession: StepSession) {
        movementDao.saveStepSession(stepSession)
    }

    override suspend fun saveStepSessions(stepSessions: List<StepSession>) {
        stepSessions.forEach { session ->
            movementDao.saveStepSession(session)
        }
    }

    override suspend fun getAllStepSessions(): List<StepSession> {
        return movementDao.getAllStepSessions()
    }

    override suspend fun clearStepSessions(stepSessions: List<StepSession>) {
        movementDao.clearStepSessions(stepSessions)
    }

    override suspend fun clearAllStepSessions() {
        movementDao.clearAllStepSessions()
    }
}