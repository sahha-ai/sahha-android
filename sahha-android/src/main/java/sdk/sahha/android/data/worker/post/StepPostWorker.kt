package sdk.sahha.android.data.worker.post

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus

private const val tag = "StepPostWorker"

class StepPostWorker(private val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        SahhaReconfigure(context)
        return postStepData()
    }

    internal suspend fun postStepData(lockTester: (() -> Unit)? = null): Result {
        val mutex = Sahha.di.sensorMutexMap[SahhaSensor.pedometer] ?: return Result.success()

        if (mutex.tryLock()) {
            lockTester?.invoke()
            try {
                Sahha.getSensorStatus(context) { _, status ->
                    Sahha.di.ioScope.launch {
                        if (status == SahhaSensorStatus.enabled) {
                            Sahha.sim.sensor.postStepDataUseCase(Sahha.di.movementDao.getAllStepData())
                        }
                    }
                }
            } finally {
                mutex.unlock()
            }
        }
        return Result.success()
    }
}

