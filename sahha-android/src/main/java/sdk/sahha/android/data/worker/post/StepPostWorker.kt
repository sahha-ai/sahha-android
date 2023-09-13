package sdk.sahha.android.data.worker.post

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha
import kotlin.coroutines.resume

private const val tag = "StepPostWorker"

class StepPostWorker(private val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        SahhaReconfigure(context)
        return postStepSessions()
    }

    internal suspend fun postStepSessions(lockTester: (() -> Unit)? = null): Result {
        // Guard: Return and do nothing if there is no auth data
        if (!Sahha.isAuthenticated) return Result.success()

        return if (Sahha.di.mutex.tryLock()) {
            lockTester?.invoke()
            try {
                suspendCancellableCoroutine<Result> { cont ->
                    Sahha.di.ioScope.launch {
                        Sahha.sim.sensor.postStepSessions { _, _ ->
                            if (cont.isActive) {
                                cont.resume(Result.success())
                            }
                        }
                    }
                }
            } finally {
                Sahha.di.mutex.unlock()
            }
        } else {
            Result.retry()
        }
    }
}

