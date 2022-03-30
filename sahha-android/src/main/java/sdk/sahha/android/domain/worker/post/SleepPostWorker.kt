package sdk.sahha.android.domain.worker.post

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class SleepPostWorker(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    override fun doWork(): Result {
        TODO("Not yet implemented")
    }
}