package sdk.sahha.android.domain.repository

import android.content.Context
import androidx.work.WorkInfo
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.device.PhoneUsageSilver
import sdk.sahha.android.domain.model.dto.SleepDto
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
        phoneLockData: List<PhoneUsage>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
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

    suspend fun saveStepSession(
        stepSession: StepSession
    )

    suspend fun getAllStepSessions(): List<StepSession>

    suspend fun clearStepSessions(
        stepSessions: List<StepSession>
    )

    suspend fun clearAllStepSessions()

    suspend fun saveStepData(
        stepData: StepData
    )

    suspend fun getAllStepData(): List<StepData>
    suspend fun clearStepData(
        stepData: List<StepData>
    )

    suspend fun clearAllStepData()
    suspend fun getExistingStepCount(totalSteps: Int): Int?
    suspend fun getAllSingleSteps(): List<StepData>
    suspend fun postStepsHourly(
        stepsHourly: List<StepSession>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )

    fun startSilverStepPostWorker(repeatIntervalMinutes: Long, workerTag: String)
    suspend fun clearStepCounterData()
    suspend fun getWorkerInfoByTag(tag: String): WorkInfo?

    // Phone Usages
    suspend fun savePhoneUsage(phoneUsage: PhoneUsage)
    suspend fun getAllPhoneUsages(): List<PhoneUsage>
    suspend fun clearPhoneUsages(phoneUsages: List<PhoneUsage>)
    suspend fun clearAllPhoneUsages()

    // Phone Usages Silver Layer
    suspend fun savePhoneUsageSilver(phoneUsage: PhoneUsageSilver)
    suspend fun getAllPhoneUsagesSilver(): List<PhoneUsageSilver>
    suspend fun clearPhoneUsagesSilver(phoneUsages: List<PhoneUsageSilver>)
    suspend fun clearAllPhoneUsagesSilver()
    fun startSilverPhoneUsagePostWorker(repeatIntervalMinutes: Long, workerTag: String)
    suspend fun postPhoneUsagesHourly(
        phoneUsagesHourly: List<PhoneUsageSilver>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    )
}