package sdk.sahha.android.domain.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.launch
import sdk.sahha.android.Sahha
import sdk.sahha.android.domain.model.device.DeviceUsage

class PhoneScreenOnReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        saveUnlockAsync()
    }

    private fun saveUnlockAsync() {
        Sahha.di.ioScope.launch {
            Sahha.di.deviceUsageDao.saveDeviceUsage(
                DeviceUsage(
                    Sahha.timeManager.nowInEpoch(), false
                )
            )
        }
    }
}
