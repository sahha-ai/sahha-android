package sdk.sahha.android.domain.repository

import okhttp3.ResponseBody
import retrofit2.Call
import sdk.sahha.android.domain.model.device_info.DeviceInformation
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.source.SahhaDemographic

interface RemoteRepo {
    suspend fun postRefreshToken(retryLogic: (suspend () -> Unit))
    suspend fun postSleepData(callback: ((error: String?, successful: Boolean) -> Unit)?)
    suspend fun postPhoneScreenLockData(callback: ((error: String?, successful: Boolean) -> Unit)?)
    suspend fun postStepData(
        stepData: List<StepData>,
        callback: ((error: String?, successful: Boolean) -> Unit)?
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