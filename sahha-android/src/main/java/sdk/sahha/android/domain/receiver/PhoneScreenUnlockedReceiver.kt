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
class PhoneScreenUnlockedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Default).launch {
            SahhaReconfigure(context)
            saveUnlockAsync(intent)
        }
    }

    private fun saveUnlockAsync(intent: Intent) {
        Sahha.di.ioScope.launch {
            Sahha.timeManager?.also { sahhaTime ->
                if (intent.action == Intent.ACTION_SCREEN_ON) {
                    Sahha.di.deviceUsageDao.saveUsage(
                        PhoneUsage(
                            isLocked = true,
                            isScreenOn = true,
                            sahhaTime.nowInISO()
                        )
                    )
                }

                if (intent.action == Intent.ACTION_USER_PRESENT) {
                    Sahha.di.deviceUsageDao.saveUsage(
                        PhoneUsage(
                            isLocked = false,
                            isScreenOn = true,
                            sahhaTime.nowInISO()
                        )
                    )
                }

            }
        }
    }
}
