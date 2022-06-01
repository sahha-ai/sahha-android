package sdk.sahha.android.domain.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.source.Sahha

@RequiresApi(Build.VERSION_CODES.O)
class PhoneScreenOffReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Default).launch {
            SahhaReconfigure(context)
            saveLockAsync()
        }
    }

    private fun saveLockAsync() {
        Sahha.di.ioScope.launch {
            Sahha.timeManager?.also { sahhaTime ->
                Sahha.di.deviceUsageDao.saveUsage(
                    PhoneUsage(
                        isLocked = true,
                        isScreenOn = false,
                        sahhaTime.nowInISO()
                    )
                )
            }
        }
    }
}
