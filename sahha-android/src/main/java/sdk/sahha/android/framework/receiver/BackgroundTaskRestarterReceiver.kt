package sdk.sahha.android.framework.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha

private const val tag = "BackgroundTaskRestarterReceiver"

class BackgroundTaskRestarterReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.Default)
    override fun onReceive(context: Context, intent: Intent) {
        scope.launch {
            try {
                SahhaReconfigure(context)
                Sahha.sim.permission.startHcOrNativeDataCollection(context.applicationContext)
            } catch (e: Exception) {
                Log.e(tag, e.message, e)
                Sahha.di.sahhaErrorLogger
                    .application(
                        e.message ?: SahhaErrors.somethingWentWrong,
                        tag,
                        "onReceive",
                        e.stackTraceToString()
                    )
            }
        }
    }

}