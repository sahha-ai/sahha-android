package sdk.sahha.android.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.service.HealthConnectPostService
import sdk.sahha.android.source.Sahha

private const val tag = "SahhaAlarmReceiver"

class SahhaAlarmReceiver: BroadcastReceiver() {
    private val ioScope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        ioScope.launch {
            try {
                SahhaReconfigure(context)
                context.startForegroundService(
                    Intent(
                        context.applicationContext,
                        HealthConnectPostService::class.java
                    )
                )
            } catch (e: Exception) {
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