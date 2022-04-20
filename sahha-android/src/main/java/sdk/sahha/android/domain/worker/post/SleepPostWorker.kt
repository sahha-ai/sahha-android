package sdk.sahha.android.domain.worker.post

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import sdk.sahha.android.Sahha
import sdk.sahha.android.common.SahhaReconfigure

class SleepPostWorker(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    override fun doWork(): Result {
        CoroutineScope(IO).launch {
            SahhaReconfigure(context.applicationContext)
            Sahha.di.postSleepDataUseCase(null)
        }
        return Result.success()
    }
}