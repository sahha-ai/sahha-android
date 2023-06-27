package sdk.sahha.android.domain.repository

import android.content.Context
import okhttp3.ResponseBody
import retrofit2.Response
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.source.SahhaSensor

interface SensorRepo {
    fun startSleepWorker(repeatIntervalMinutes: Long, workerTag: String)
    fun startPostWorkersAsync()
    fun startSleepPostWorker(repeatIntervalMinutes: Long, workerTag: String)
    fun startDevicePostWorker(repeatIntervalMinutes: Long, workerTag: String)
    fun startStepPostWorker(repeatIntervalMinutes: Long, workerTag: String)
    fun stopWorkerByTag(workerTag: String)
    fun stopAllWorkers()
    suspend fun startStepDetectorAsync(
        context: Context,
        movementDao: MovementDao,
    )

    suspend fun startStepCounterAsync(
        context: Context,
        movementDao: MovementDao,
    )

    suspend fun getSensorData(
        sensor: SahhaSensor,
        callback: ((error: String?, successful: String?) -> Unit)
    )

    suspend fun postSleepData(
        sleepData: List<SleepDto>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun postPhoneScreenLockData(
        phoneLockData: List<PhoneUsage>, callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun postStepData(
        stepData: List<StepData>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun postAllSensorData(
        callback: ((error: String?, successful: Boolean) -> Unit)
    )

    suspend fun startPostWorkersHealthConnect()
    suspend fun <T> postData(
        data: List<T>,
        sensor: SahhaSensor,
        chunkLimit: Int,
        getResponse: suspend (List<T>) -> Response<ResponseBody>,
        clearData: suspend (List<T>) -> Unit,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun <T> sendChunk(
        chunk: List<T>,
        getResponse: suspend (List<T>) -> Response<ResponseBody>,
        clearData: suspend (List<T>) -> Unit
    ): Boolean

    suspend fun handleException(
        e: Exception,
        functionName: String,
        data: String,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )
}