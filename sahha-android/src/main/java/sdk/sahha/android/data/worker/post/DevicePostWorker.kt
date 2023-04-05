package sdk.sahha.android.data.worker.post

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor

private const val tag = "DevicePostWorker"

class DevicePostWorker(private val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        SahhaReconfigure(context)
        return postDeviceData()
    }

    internal suspend fun postDeviceData(lockTester: (() -> Unit)? = null): Result {
        val mutex = Sahha.di.sensorMutexMap[SahhaSensor.device] ?: return Result.success()
        if (mutex.tryLock()) {
            lockTester?.invoke()
            try {
                Sahha.di.postDeviceDataUseCase()
            } finally {
                mutex.unlock()
            }
        }

        return Result.success()
    }
}