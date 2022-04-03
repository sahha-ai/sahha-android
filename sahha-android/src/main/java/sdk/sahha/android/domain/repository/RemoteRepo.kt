package sdk.sahha.android.domain.repository

interface RemoteRepo {
    suspend fun postSleepData(callback: ((error: String?, successful: String?) -> Unit)?)
    suspend fun postPhoneScreenLockData(callback: ((error: String?, successful: String?) -> Unit)?)
    suspend fun getAnalysis(callback: ((error: String?, successful: String?) -> Unit)?)
}