package sdk.sahha.android.framework.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.source.Sahha

internal class PhoneScreenStateReceiver : BroadcastReceiver() {
    private val ioScope by lazy { CoroutineScope(IO) }

    override fun onReceive(context: Context, intent: Intent) {
        ioScope.launch {
            if (!Sahha.diInitialized()) SahhaReconfigure(context)
            saveScreenStateAsync()
        }
    }

    private suspend fun saveScreenStateAsync() {
        Sahha.di.timeManager.also { sahhaTime ->
            Sahha.di.deviceUsageRepo.saveUsages(
                listOf(
                    PhoneUsage(
                        isLocked = Sahha.di.keyguardManager.isKeyguardLocked,
                        isScreenOn = Sahha.di.powerManager.isInteractive,
                        sahhaTime.nowInISO()
                    )
                )
            )
        }
    }
}
