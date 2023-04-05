package sdk.sahha.android.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
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
import sdk.sahha.android.data.remote.dto.StepDto
import sdk.sahha.android.data.worker.SleepCollectionWorker
import sdk.sahha.android.data.worker.post.DevicePostWorker
import sdk.sahha.android.data.worker.post.SleepPostWorker
import sdk.sahha.android.data.worker.post.StepPostWorker
import sdk.sahha.android.domain.model.config.toSetOfSensors
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
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        try {
            if (stepData.isEmpty()) {
                callback?.also { it(SahhaErrors.localDataIsEmpty(SahhaSensor.pedometer), false) }
                return
            }

            val stepDtoData = SahhaConverterUtility.stepDataToStepDto(getFilteredStepData(stepData))
            val response = getStepResponse(stepDtoData) ?: return
            handleResponse(response, { getStepResponse(stepDtoData) }, callback) {
                if (stepData.count() > Constants.MAX_STEP_POST_VALUE)
                    movementDao.clearFirstStepData(Constants.MAX_STEP_POST_VALUE)
                else clearLocalStepData()
            }
        } catch (e: Exception) {
            callback?.also { it(e.message, false) }

            sahhaErrorLogger.application(
                e.message,
                "postStepData",
                stepData.toString()
            )
        }
    }

    override suspend fun postSleepData(callback: ((error: String?, successful: Boolean) -> Unit)?) {
        try {
            if (sleepDao.getSleepDto().isEmpty()) {
                callback?.also { it(SahhaErrors.localDataIsEmpty(SahhaSensor.sleep), false) }
                return
            }

            val response = getSleepResponse()
            handleResponse(response, { getSleepResponse() }, callback) {
                clearLocalSleepData()
            }
        } catch (e: Exception) {
            callback?.also { it(e.message, false) }

            sahhaErrorLogger.application(
                e.message,
                "postSleepData",
                sleepDao.getSleepDto().toString()
            )
        }
    }

    override suspend fun postPhoneScreenLockData(callback: ((error: String?, successful: Boolean) -> Unit)?) {
        try {
            if (deviceDao.getUsages().isEmpty()) {
                callback?.also { it(SahhaErrors.localDataIsEmpty(SahhaSensor.device), false) }
                return
            }

            val call = getPhoneScreenLockResponse()
            handleResponse(call, { getPhoneScreenLockResponse() }, callback) {
                clearLocalPhoneScreenLockData()
            }
        } catch (e: Exception) {
            callback?.also { it(e.message, false) }

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
            callback?.also { it(null, "{}") }
            return
        }

        val reader = response.body()?.charStream()
        val bodyString = reader?.readText()
        val json = JSONObject(bodyString ?: "")
        val jsonString = json.toString(6)
        callback?.also { it(null, jsonString) }
    }

    private suspend fun handleResponse(
        call: Call<ResponseBody>,
        retryLogic: suspend (() -> Call<ResponseBody>),
        callback: ((error: String?, successful: Boolean) -> Unit)?,
        successfulLogic: (suspend () -> Unit)
    ) {
        call.enqueue(
            object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    ioScope.launch {
                        if (ResponseCode.isUnauthorized(response.code())) {
                            callback?.also { it(SahhaErrors.attemptingTokenRefresh, false) }
                            SahhaResponseHandler.checkTokenExpired(response.code()) {
                                val retryResponse = retryLogic()
                                handleResponse(
                                    retryResponse,
                                    retryLogic,
                                    callback,
                                    successfulLogic
                                )
                            }
                            return@launch
                        }

                        if (ResponseCode.isSuccessful(response.code())) {
                            successfulLogic()
                            callback?.also {
                                it(null, true)
                            }
                            return@launch
                        }

                        callback?.also {
                            it(
                                "${response.code()}: ${response.message()}",
                                false
                            )
                        }

                        sahhaErrorLogger.api(call, response)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    callback?.also { it(t.message, false) }

                    sahhaErrorLogger.api(
                        call,
                        SahhaErrors.typeResponse,
                        null,
                        t.message ?: SahhaErrors.responseFailure
                    )
                }
            }
        )
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
                val mutex = Sahha.di.sensorMutexMap[sensor]
                if (mutex != null) {
                    if (mutex.tryLock()) {
                        try {
                            when (sensor) {
                                SahhaSensor.sleep -> {
                                    postSleepData { error, successful ->
                                        error?.also { errorSummary += "$it\n" }
                                        reschedulWorker(SahhaSensor.sleep)
                                        successfulResults.add(successful)
                                        deferredResult.complete(Unit)
                                    }
                                }
                                SahhaSensor.device -> {
                                    postPhoneScreenLockData { error, successful ->
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

    private fun getStepResponse(stepData: List<StepDto>): Call<ResponseBody> {
        val token = authRepo.getToken()!!
        return api.postStepData(
            TokenBearer(token),
            stepData
        )
    }

    private suspend fun getSleepResponse(): Call<ResponseBody> {
        val token = authRepo.getToken()!!
        return api.postSleepDataRange(
            TokenBearer(token),
            SahhaConverterUtility.sleepDtoToSleepSendDto(sleepDao.getSleepDto())
        )
    }

    private suspend fun getPhoneScreenLockResponse(): Call<ResponseBody> {
        val token = authRepo.getToken()!!
        return api.postDeviceActivityRange(
            TokenBearer(token),
            SahhaConverterUtility.phoneUsageToPhoneUsageSendDto(deviceDao.getUsages())
        )
    }


}