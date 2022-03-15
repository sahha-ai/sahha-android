package sdk.sahha.android.domain.repository

interface BackgroundRepo {
    fun startDataCollectionService()
    fun startActivityRecognitionReceiver()
    fun startPhoneScreenReceiver()
    fun startStepWorker(repeatIntervalMinutes: Long, workerTag: String)
    fun stopWorkerByTag(workerTag: String)
    fun stopAllWorkers()
}