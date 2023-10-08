package sdk.sahha.android.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.data.service.HealthConnectPostService
import sdk.sahha.android.source.Sahha

private const val tag = "SahhaAlarmReceiver"

class SahhaAlarmReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val notificationManager by lazy { Sahha.di.sahhaNotificationManager }

    override fun onReceive(context: Context, intent: Intent) {
        scope.launch {
            try {
                SahhaReconfigure(context)
                notificationManager.startHealthConnectPostService()
                notificationManager.startDataCollectionService(null, null, null, null)

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