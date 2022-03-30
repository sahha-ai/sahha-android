package sdk.sahha.android.domain.worker.post

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.launch
import sdk.sahha.android.Sahha

class SleepPostWorker(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    override fun doWork(): Result {
        Sahha.di.defaultScope.launch {

        }
        return Result.success()
    }
}