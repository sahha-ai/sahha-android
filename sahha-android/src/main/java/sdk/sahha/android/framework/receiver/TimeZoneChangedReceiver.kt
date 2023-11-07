package sdk.sahha.android.framework.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha

private const val tag = "TimeZoneChangedReceiver"

class TimeZoneChangedReceiver : BroadcastReceiver() {
    private val ioScope by lazy { CoroutineScope(IO) }

    override fun onReceive(context: Context, intent: Intent) {
        ioScope.launch {
            SahhaReconfigure(context)
            Sahha.sim.userData.processAndPutDeviceInfo(context)
            Log.d(tag, "Timezone changed: ${Sahha.di.timeManager.getTimezone()}")
        }
    }
}