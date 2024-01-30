package sdk.sahha.android.framework.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.source.Sahha

@RequiresApi(Build.VERSION_CODES.O)
internal class PhoneScreenStateReceiver : BroadcastReceiver() {
    private val ioScope by lazy { CoroutineScope(IO) }

    override fun onReceive(context: Context, intent: Intent) {
        ioScope.launch {
            SahhaReconfigure(context)
            saveScreenStateAsync()
        }
    }

    private suspend fun saveScreenStateAsync() {
        Sahha.di.timeManager.also { sahhaTime ->
            Sahha.di.deviceUsageDao.saveUsage(
                PhoneUsage(
                    isLocked = Sahha.di.keyguardManager.isKeyguardLocked,
                    isScreenOn = Sahha.di.powerManager.isInteractive,
                    sahhaTime.nowInISO()
                )
            )
        }
    }
}
