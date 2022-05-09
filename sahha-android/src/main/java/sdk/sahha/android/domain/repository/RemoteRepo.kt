package sdk.sahha.android.domain.repository

import sdk.sahha.android.source.SahhaDemographic
import sdk.sahha.android.source.SahhaSensor

interface RemoteRepo {
    suspend fun postRefreshToken(retryLogic: (suspend () -> Unit))
    suspend fun postSleepData(callback: ((error: String?, successful: Boolean) -> Unit)?)
    suspend fun postPhoneScreenLockData(callback: ((error: String?, successful: Boolean) -> Unit)?)
    suspend fun postAllSensorData(sensors: Set<Enum<SahhaSensor>>?, callback: ((error: String?, successful: Boolean) -> Unit))
    suspend fun getAnalysis(
        callback: ((error: String?, successful: String?) -> Unit)?,
        startDate: String? = null,
        endDate: String? = null
    )
    suspend fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?)
    suspend fun postDemographic(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, successful: Boolean) -> Unit)?
    )
}