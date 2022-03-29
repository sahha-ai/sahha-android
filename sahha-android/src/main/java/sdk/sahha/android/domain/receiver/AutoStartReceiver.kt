package sdk.sahha.android.domain.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.launch
import sdk.sahha.android.Sahha

class AutoStartReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Sahha.di.defaultScope.launch {
            // TODO:("Restart service and workers")
        }
    }
}