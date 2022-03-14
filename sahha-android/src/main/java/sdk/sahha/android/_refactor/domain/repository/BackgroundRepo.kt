package sdk.sahha.android._refactor.domain.repository

interface BackgroundRepo {
    suspend fun startDataCollectionService()
    suspend fun startActivityRecognitionReceiver()
    suspend fun startPhoneScreenReceiver()
    suspend fun startStepWorker(repeatIntervalMinutes: Long, workerTag: String)
    suspend fun stopWorkerByTag(workerTag: String)
    suspend fun stopAllWorkers()
}