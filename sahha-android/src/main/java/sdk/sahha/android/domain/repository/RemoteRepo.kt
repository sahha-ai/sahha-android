package sdk.sahha.android.domain.repository

import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord
import sdk.sahha.android.common.enums.HealthConnectSensor
import sdk.sahha.android.data.remote.dto.SleepDto
import sdk.sahha.android.data.remote.dto.send.HeartRateSendDto
import sdk.sahha.android.data.remote.dto.send.StepSendDto
import sdk.sahha.android.domain.model.device_info.DeviceInformation
import sdk.sahha.android.source.SahhaDemographic

interface RemoteRepo {
    suspend fun postSleepData(
        sleepData: List<SleepDto>,
        callback: ((error: String?, successful: Boolean) -> Unit)? = null
    )

    suspend fun postPhoneScreenLockData(callback: ((error: String?, successful: Boolean) -> Unit)? = null)
    suspend fun postStepData(
        stepData: List<StepSendDto>,
        callback: ((error: String?, successful: Boolean) -> Unit)? = null
    )

    suspend fun postHeartRateData(
        heartRateData: List<HeartRateSendDto>,
        callback: ((error: String?, successful: Boolean) -> Unit)? = null
    )

    suspend fun postAllSensorData(
        callback: ((error: String?, successful: Boolean) -> Unit)
    )

    suspend fun getAnalysis(
        dates: Pair<String, String>? = null,
        includeSourceData: Boolean,
        callback: ((error: String?, successful: String?) -> Unit)?,
    )

    suspend fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?)
    suspend fun postDemographic(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun putDeviceInformation(deviceInformation: DeviceInformation)
}