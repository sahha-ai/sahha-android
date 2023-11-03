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
import sdk.sahha.android.framework.service.InsightsPostService
import sdk.sahha.android.source.Sahha

private const val tag = "InsightsQueryReceiver"

class InsightsQueryReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val nm by lazy { Sahha.di.sahhaNotificationManager }

    override fun onReceive(context: Context, intent: Intent) {
        scope.launch {
            try {
                SahhaReconfigure(context)
                nm.startForegroundService(InsightsPostService::class.java)
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