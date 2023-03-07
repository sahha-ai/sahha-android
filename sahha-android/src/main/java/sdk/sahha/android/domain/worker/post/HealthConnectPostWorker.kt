package sdk.sahha.android.domain.worker.post

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha

class HealthConnectPostWorker(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    override fun doWork(): Result {
        CoroutineScope(Dispatchers.IO).launch {
            SahhaReconfigure(context)
            Sahha.di.healthConnectRepo?.also {
                Sahha.di.remotePostRepo.postHealthConnectData(
                    it.getSleepData(),
                    it.getSleepStageData(),
                    it.getStepData(),
                    it.getHeartRateData()
                )
            }
        }

        return Result.success()
    }
}