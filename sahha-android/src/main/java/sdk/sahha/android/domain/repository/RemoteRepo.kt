package sdk.sahha.android.domain.repository

import sdk.sahha.android.domain.model.profile.SahhaDemographic

interface RemoteRepo {
    suspend fun postRefreshToken(retryLogic: (() -> Unit))
    suspend fun postSleepData(callback: ((error: String?, successful: String?) -> Unit)?)
    suspend fun postPhoneScreenLockData(callback: ((error: String?, successful: String?) -> Unit)?)
    suspend fun getAnalysis(callback: ((error: String?, successful: String?) -> Unit)?)
    suspend fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?)
    suspend fun postDemographic(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, successful: String?) -> Unit)?
    )
}