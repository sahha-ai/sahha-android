package sdk.sahha.android.framework.worker.post

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha
import kotlin.coroutines.resume

private const val tag = "StepPostWorker"

internal class StepPostWorker(private val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        SahhaReconfigure(context)
        return postStepSessions()
    }

    internal suspend fun postStepSessions(lockTester: (suspend () -> Unit)? = null): Result {
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
                try {
                    Sahha.di.mutex.unlock()
                } catch (e: Exception) {
                    Log.w(tag, e.message ?: "Failed to unlock mutex")
                    Result.retry()
                }
            }
        } else {
            Result.retry()
        }
    }
}

