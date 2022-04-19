package sdk.sahha.android.domain.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.launch
import sdk.sahha.android.Sahha
import sdk.sahha.android.domain.model.device.PhoneUsage

@RequiresApi(Build.VERSION_CODES.O)
class PhoneScreenUnlockedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        saveUnlockAsync()
    }

    private fun saveUnlockAsync() {
        Sahha.di.ioScope.launch {
            Sahha.di.deviceUsageDao.saveUsage(
                PhoneUsage(
                    false, Sahha.timeManager!!.nowInISO()
                )
            )
        }
    }
}
