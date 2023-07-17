package sdk.sahha.android.domain.repository

import android.content.Context
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.model.dto.StepDto
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.StepSession
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

    suspend fun postStepSessions(
        stepSessions: List<StepSession>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun postAllSensorData(
        callback: ((error: String?, successful: Boolean) -> Unit)
    )

    suspend fun storeStepSession(
        stepSession: StepSession
    )

    suspend fun getAllStepSessions(): List<StepSession>

    suspend fun clearStepSessions(
        stepSessions: List<StepSession>
    )

    suspend fun clearAllStepSessions()
}