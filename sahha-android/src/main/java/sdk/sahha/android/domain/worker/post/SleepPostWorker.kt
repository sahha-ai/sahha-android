package sdk.sahha.android.domain.worker.post

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha

class SleepPostWorker(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    override fun doWork(): Result {
        CoroutineScope(IO).launch {
            SahhaReconfigure(context)
            val sleepData = Sahha.di.sleepDao.getSleepDto()
            Sahha.di.remotePostRepo.postSleepData(sleepData) { error, _ ->
                error?.also { e ->
                    Sahha.di.sahhaErrorLogger.application(
                        error = e,
                        appMethod = "SleepPostWorker, doWork",
                        appBody = sleepData.toString()
                    )
                }
            }
        }
        return Result.success()
    }
}