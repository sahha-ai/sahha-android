package sdk.sahha.android.framework.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha

class AutoStartReceiver : BroadcastReceiver() {
    private val defaultScope = CoroutineScope(Default)
    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED
            || intent.action == Intent.ACTION_BOOT_COMPLETED
        ) {
            defaultScope.launch {
                SahhaReconfigure(context)
                Sahha.sim.permission.startHcOrNativeDataCollection(context.applicationContext)
            }
        }
    }
}