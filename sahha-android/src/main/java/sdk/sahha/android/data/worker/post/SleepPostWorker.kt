package sdk.sahha.android.data.worker.post

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor

class SleepPostWorker(private val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        SahhaReconfigure(context)
        return postSleepData()
    }

    internal suspend fun postSleepData(lockTester: (() -> Unit)? = null): Result {
        val mutex = Sahha.di.sensorMutexMap[SahhaSensor.sleep] ?: return Result.success()

        if (mutex.tryLock()) {
            lockTester?.invoke()
            try {
                Sahha.di.postSleepDataUseCase()
            } finally {
                mutex.unlock()
            }
        }
        return Result.success()
    }
}