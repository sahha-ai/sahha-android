package sdk.sahha.android.domain.repository

interface RemotePostRepo {
    suspend fun postSleepData(callback: ((error: String?, successful: String?) -> Unit)?)
    suspend fun postPhoneScreenLockData(callback: ((error: String?, successful: String?) -> Unit)?)
}