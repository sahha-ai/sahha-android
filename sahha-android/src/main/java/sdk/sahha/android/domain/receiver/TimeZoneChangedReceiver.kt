package sdk.sahha.android.domain.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha

private const val tag = "TimeZoneChangedReceiver"

class TimeZoneChangedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.Default).launch {
            SahhaReconfigure(context)
            Sahha.processAndPutDeviceInfo(context)
            Log.d(tag, "Timezone changed: ${Sahha.timeManager.getTimezone()}")
        }
    }
}