package sdk.sahha.android.domain.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.common.SahhaReconfigure

class AutoStartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED
            || intent.action == Intent.ACTION_BOOT_COMPLETED
        ) {
            CoroutineScope(Default).launch {
                SahhaReconfigure(context)
                Sahha.start()
            }
        }
    }
}