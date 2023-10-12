package sdk.sahha.android.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha

private const val tag = "SahhaAlarmReceiver"

class SahhaAlarmReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val nm by lazy { Sahha.di.sahhaNotificationManager }
    private val pm by lazy { Sahha.di.permissionManager }

    override fun onReceive(context: Context, intent: Intent) {
        scope.launch {
            try {
                SahhaReconfigure(context)

                when (pm.shouldUseHealthConnect()) {
                    true -> nm.startHealthConnectPostService()
                    false -> nm.startDataCollectionService()
                }

                this.cancel()
            } catch (e: Exception) {
                Log.e(tag, e.message, e)
                Sahha.di.sahhaErrorLogger
                    .application(
                        e.message ?: SahhaErrors.somethingWentWrong,
                        tag,
                        "onReceive",
                        e.stackTraceToString()
                    )
                this.cancel()
            }
        }
    }
}