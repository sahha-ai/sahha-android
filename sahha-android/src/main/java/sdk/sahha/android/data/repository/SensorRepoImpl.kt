package sdk.sahha.android.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import sdk.sahha.android.common.*
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.Constants.DEVICE_POST_WORKER_TAG
import sdk.sahha.android.data.Constants.SLEEP_POST_WORKER_TAG
import sdk.sahha.android.data.Constants.STEP_POST_WORKER_TAG
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.data.local.dao.DeviceUsageDao
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.data.local.dao.SleepDao
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.worker.SleepCollectionWorker
import sdk.sahha.android.data.worker.post.DevicePostWorker
import sdk.sahha.android.data.worker.post.SleepPostWorker
import sdk.sahha.android.data.worker.post.StepPostWorker
import sdk.sahha.android.domain.model.config.toSetOfSensors
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.model.dto.StepDto
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.source.*
import java.util.concurrent.TimeUnit

private const val tag = "SensorRepoImpl"

@SuppressLint("NewApi")
class SensorRepoImpl(
    private val context: Context,
    private val defaultScope: CoroutineScope,
    private val ioScope: CoroutineScope,
    private val configDao: ConfigurationDao,
    private val deviceDao: DeviceUsageDao,
    private val sleepDao: SleepDao,
    private val movementDao: MovementDao,
    private val authRepo: AuthRepo,
    private val sahhaErrorLogger: SahhaErrorLogger,
    private val sensorMutexMap: Map<SahhaSensor, Mutex>,
    private val api: SahhaApi

) : SensorRepo {
    private val workManager by lazy { WorkManager.getInstance(context) }
    private val sensorToWorkerAction = mapOf(
        SahhaSensor.sleep to Pair(SLEEP_POST_WORKER_TAG, ::startSleepPostWorker),
        SahhaSensor.device to Pair(DEVICE_POST_WORKER_TAG, ::startDevicePostWorker),
        SahhaSensor.pedometer to Pair(STEP_POST_WORKER_TAG, ::startStepPostWorker)
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
        Sahha.getSensorStatus(
            context,
        ) { _, status ->
            if (status == SahhaSensorStatus.enabled) {
                val checkedIntervalMinutes = getCheckedIntervalMinutes(repeatIntervalMinutes)
                val workRequest: PeriodicWorkRequest =
                    getSleepWorkRequest(checkedIntervalMinutes, workerTag)
                startWorkManager(workRequest, workerTag, ExistingPeriodicWorkPolicy.REPLACE)
            }
        }
    }

    override fun startPostWorkersAsync() {
        defaultScope.launch {
            val config = configDao.getConfig()
            Sahha.getSensorStatus(
                context,
            ) { _, status ->
                if (config.sensorArray.contains(SahhaSensor.device.ordinal)) {
                    startDevicePostWorker(360, DEVICE_POST_WORKER_TAG)
                }

                if (status == SahhaSensorStatus.enabled) {
                    if (config.sensorArray.contains(SahhaSensor.sleep.ordinal)) {
                        startSleepPostWorker(360, SLEEP_POST_WORKER_TAG)
                    }
                    if (config.sensorArray.contains(SahhaSensor.pedometer.ordinal)) {
                        startStepPostWorker(15, STEP_POST_WORKER_TAG)
                    }
                }
            }
        }
    }

    override fun stopWorkerByTag(workerTag: String) {
        workManager.cancelAllWorkByTag(workerTag)
    }

    override fun stopAllWorkers() {
        workManager.cancelAllWork()
    }

    override suspend fun getSensorData(
        sensor: SahhaSensor,
        callback: ((error: String?, successful: String?) -> Unit)
    ) {
        try {
            when (sensor) {
                SahhaSensor.device -> {
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
                SahhaSensor.pedometer -> {
                    val stepSummary = getStepDataSummary()
                    if (stepSummary.isNotEmpty()) {
                        callback(null, stepSummary)
                        return
                    }
                }
            }
            callback("No data found", null)
        } catch (e: Exception) {
            callback("Error: " + e.message, null)
        }
    }

    private suspend fun getDeviceDataSummary(): String {
        var dataSummary = ""
        deviceDao.getUsages().forEach {
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

    private fun getFilteredStepData(stepData: List<StepData>): List<StepData> {
        return if (stepData.count() > 1000) {
            stepData.subList(0, 1000)
        } else stepData
    }

    override suspend fun postStepData(
        stepData: List<StepData>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        try {
            if (stepData.isEmpty()) {
                callback?.invoke(SahhaErrors.localDataIsEmpty(SahhaSensor.pedometer), false)
                return
            }

            val stepDtoData = SahhaConverterUtility.stepDataToStepDto(getFilteredStepData(stepData))
            val response = getStepResponse(stepDtoData) ?: return
            Log.d(
                tag,
                "Content length step: " + response.raw().request.body?.contentLength().toString()
            )
            handleResponse(response, { getStepResponse(stepDtoData) }, callback)
        } catch (e: Exception) {
            callback?.invoke(e.message, false)

            sahhaErrorLogger.application(
                e.message,
                "postStepData",
                stepData.toString()
            )
        }
    }

    override suspend fun postSleepData(
        sleepData: List<SleepDto>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        try {
            if (sleepData.isEmpty()) {
                callback?.invoke(SahhaErrors.localDataIsEmpty(SahhaSensor.sleep), false)
                return
            }

            val response = getSleepResponse(sleepData)
            Log.d(
                tag,
                "Content length sleep: " + response.raw().request.body?.contentLength().toString()
            )
            handleResponse(response, { getSleepResponse(sleepData) }, callback)
        } catch (e: Exception) {
            callback?.invoke(e.message, false)

            sahhaErrorLogger.application(
                e.message,
                "postSleepData",
                sleepDao.getSleepDto().toString()
            )
        }
    }

    override suspend fun postPhoneScreenLockData(
        phoneLockData: List<PhoneUsage>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        try {
            if (phoneLockData.isEmpty()) {
                callback?.invoke(SahhaErrors.localDataIsEmpty(SahhaSensor.device), false)
                return
            }

            val response = getPhoneScreenLockResponse(phoneLockData)
            Log.d(
                tag,
                "Content length phone lock: " + response.raw().request.body?.contentLength()
                    .toString()
            )
            handleResponse(response, { getPhoneScreenLockResponse(phoneLockData) }, callback)
        } catch (e: Exception) {
            callback?.invoke(e.message, false)

            sahhaErrorLogger.application(
                e.message,
                "postPhoneScreenLockData",
                deviceDao.getUsages().toString()
            )
        }
    }

    override suspend fun postAllSensorData(
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        try {
            postSensorData(callback)
        } catch (e: Exception) {
            callback(e.message, false)
            sahhaErrorLogger.application(
                e.message,
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

            sahhaErrorLogger.api(response, SahhaErrors.typeResponse)
        } catch (e: Exception) {
            callback?.also {
                it(e.message, false)
            }

            sahhaErrorLogger.application(
                e.message,
                "handleResponse",
                response.message(),
            )
        }
    }

    private suspend fun postSensorData(
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        var errorSummary = ""
        val successfulResults = mutableListOf<Boolean>()

        val sensors = Sahha.di.configurationDao.getConfig().toSetOfSensors()
            .ifEmpty { SahhaSensor.values().toSet() }
        val jobs = sensors.map { sensor ->
            ioScope.async {
                val deferredResult = CompletableDeferred<Unit>()
                val mutex = sensorMutexMap[sensor]
                if (mutex != null) {
                    if (mutex.tryLock()) {
                        try {
                            when (sensor) {
                                SahhaSensor.sleep -> {
                                    postSleepData(sleepDao.getSleepDto()) { error, successful ->
                                        error?.also { errorSummary += "$it\n" }
                                        reschedulWorker(SahhaSensor.sleep)
                                        successfulResults.add(successful)
                                        deferredResult.complete(Unit)
                                    }
                                }
                                SahhaSensor.device -> {
                                    postPhoneScreenLockData(deviceDao.getUsages()) { error, successful ->
                                        error?.also { errorSummary += "$it\n" }
                                        reschedulWorker(SahhaSensor.device)
                                        successfulResults.add(successful)
                                        deferredResult.complete(Unit)
                                    }
                                }
                                SahhaSensor.pedometer -> {
                                    postStepData(movementDao.getAllStepData()) { error, successful ->
                                        error?.also { errorSummary += "$it\n" }
                                        reschedulWorker(SahhaSensor.pedometer)
                                        successfulResults.add(successful)
                                        deferredResult.complete(Unit)
                                    }
                                }
                            }
                        } finally {
                            mutex.unlock()
                        }
                    } else {
                        // Mutex is locked, so a posting is already in progress
                        errorSummary += "${SahhaErrors.postingInProgress}\n"
                        successfulResults.add(false)
                        deferredResult.complete(Unit)
                    }
                }
                deferredResult.await()
            }
        }

        jobs.awaitAll()


        if (successfulResults.contains(false)) {
            callback(errorSummary, false)
            return
        }

        callback(null, true)
    }

    private fun reschedulWorker(sensor: Enum<SahhaSensor>) {
        sensorToWorkerAction[sensor]?.let { (workerTag, startWorkerAction) ->
            stopWorkerByTag(workerTag)
            startWorkerAction(Constants.WORKER_REPEAT_INTERVAL_MINUTES, workerTag)
        }
    }

    private suspend fun clearLocalStepData() {
        movementDao.clearAllStepData()
    }

    private suspend fun clearLocalSleepData() {
        sleepDao.clearSleepDto()
        sleepDao.clearSleep()
    }

    private suspend fun clearLocalPhoneScreenLockData() {
        deviceDao.clearUsages()
    }

    private suspend fun getStepResponse(stepData: List<StepDto>): Response<ResponseBody> {
        val token = authRepo.getToken()!!
        return api.postStepData(
            TokenBearer(token),
            stepData
        )
    }

    private suspend fun getSleepResponse(sleepData: List<SleepDto>): Response<ResponseBody> {
        val token = authRepo.getToken()!!
        return api.postSleepDataRange(
            TokenBearer(token),
            SahhaConverterUtility.sleepDtoToSleepSendDto(sleepData)
        )
    }

    private suspend fun getPhoneScreenLockResponse(phoneLockData: List<PhoneUsage>): Response<ResponseBody> {
        val token = authRepo.getToken()!!
        return api.postDeviceActivityRange(
            TokenBearer(token),
            SahhaConverterUtility.phoneUsageToPhoneUsageSendDto(phoneLockData)
        )
    }


}