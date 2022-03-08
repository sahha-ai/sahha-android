package sdk.sahha.android.controller.workers

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class StepWorker(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    private val mainScope by lazy { CoroutineScope(Main) }

    override fun doWork(): Result {
        Log.w("StepWorker", "Step worker successful")
        mainScope.launch {
            Toast.makeText(context, "Step worker test", Toast.LENGTH_LONG).show()
        }
        return Result.success()
    }
}
