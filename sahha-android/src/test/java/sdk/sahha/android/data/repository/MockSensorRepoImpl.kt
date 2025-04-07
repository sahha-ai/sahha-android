package sdk.sahha.android.data.repository

import android.content.Context
import okhttp3.ResponseBody
import retrofit2.Response
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.StepSession
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.source.SahhaSensor

internal class MockSensorRepoImpl: SensorRepo {
    override fun startSleepWorker(repeatIntervalMinutes: Long, workerTag: String) {
        TODO("Not yet implemented")
    }

    override fun startSleepPostWorker(repeatIntervalMinutes: Long, workerTag: String) {
        TODO("Not yet implemented")
    }

    override fun startDevicePostWorker(repeatIntervalMinutes: Long, workerTag: String) {
        TODO("Not yet implemented")
    }

    override fun startStepPostWorker(repeatIntervalMinutes: Long, workerTag: String) {
        TODO("Not yet implemented")
    }

    override fun stopWorkerByTag(workerTag: String) {
        TODO("Not yet implemented")
    }

    override fun stopAllWorkers() {
        TODO("Not yet implemented")
    }

    override suspend fun startStepDetectorAsync(context: Context, movementDao: MovementDao) {
        TODO("Not yet implemented")
    }

    override suspend fun startStepCounterAsync(context: Context, movementDao: MovementDao) {
        TODO("Not yet implemented")
    }

    override suspend fun getSensorData(
        sensor: SahhaSensor,
        callback: suspend (error: String?, successful: String?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun postSleepData(
        sleepData: List<SleepDto>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun postPhoneScreenLockData(
        phoneLockData: List<PhoneUsage>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun postStepData(
        stepData: List<StepData>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun postStepSessions(
        stepSessions: List<StepSession>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun postAllSensorData(callback: ((error: String?, successful: Boolean) -> Unit)?) {
        TODO("Not yet implemented")
    }

    private val stepSessions = mutableListOf<StepSession>()
    override suspend fun saveStepSession(stepSession: StepSession) {
        this.stepSessions += stepSession
    }

    override suspend fun saveStepSessions(stepSessions: List<StepSession>) {
        this.stepSessions += stepSessions
    }

    override suspend fun getAllStepSessions(): List<StepSession> {
        return this.stepSessions
    }

    override suspend fun clearStepSessions(stepSessions: List<StepSession>) {
        this.stepSessions.removeAll(stepSessions)
    }

    override suspend fun clearAllStepSessions() {
        this.stepSessions.clear()
    }

    override suspend fun <T> postData(
        data: List<T>,
        sensor: SahhaSensor,
        chunkLimit: Int,
        getResponse: suspend (List<T>) -> Response<ResponseBody>,
        clearData: suspend (List<T>) -> Unit,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun <T> sendChunk(
        chunk: List<T>,
        getResponse: suspend (List<T>) -> Response<ResponseBody>,
        clearData: suspend (List<T>) -> Unit
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun handleException(
        e: Exception,
        functionName: String,
        data: String,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override fun checkAndStartWorker(
        config: SahhaConfiguration,
        sensorId: Int,
        startWorker: () -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun startBatchedDataPostWorker(repeatIntervalMinutes: Long, workerTag: String) {
        TODO("Not yet implemented")
    }

    override fun startHealthConnectQueryWorker(repeatIntervalMinutes: Long, workerTag: String) {
        TODO("Not yet implemented")
    }

    override fun startBackgroundTaskRestarterWorker(
        repeatIntervalMinutes: Long,
        workerTag: String
    ) {
        TODO("Not yet implemented")
    }

    override fun startOneTimeBatchedDataPostWorker(workerTag: String) {
        TODO("Not yet implemented")
    }
}