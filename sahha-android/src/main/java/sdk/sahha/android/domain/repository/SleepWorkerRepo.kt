package sdk.sahha.android.domain.repository

interface SleepWorkerRepo {
    suspend fun postSleepData(callback: ((responseSuccessful: Boolean) -> Unit))
}