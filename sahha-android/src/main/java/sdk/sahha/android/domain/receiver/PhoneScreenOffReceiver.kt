package sdk.sahha.android.domain.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.launch
import sdk.sahha.android.Sahha
import sdk.sahha.android.domain.model.device.PhoneUsage

class PhoneScreenOffReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        saveLockAsync()
    }

    private fun saveLockAsync() {
        Sahha.di.ioScope.launch {
            Sahha.di.deviceUsageDao.saveUsage(
                PhoneUsage(
                    true,
                    Sahha.timeManager.nowInISO()
                )
            )
        }
    }
}
