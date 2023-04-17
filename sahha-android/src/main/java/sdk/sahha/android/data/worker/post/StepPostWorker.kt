package sdk.sahha.android.data.worker.post

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val tag = "StepPostWorker"
private const val STEP_CHUNK_LIMIT = 37

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
                            val data = Sahha.di.movementDao.getAllStepData()
                            Sahha.di.postChunkManager.postAllChunks(
                                data, STEP_CHUNK_LIMIT
                            ) { chunkedData ->
                                suspendCoroutine { cont ->
                                    launch {
                                        Sahha.sim.sensor.postStepDataUseCase(chunkedData) { error, success ->
                                            cont.resume(success)
                                        }
                                    }
                                }
                            }
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

