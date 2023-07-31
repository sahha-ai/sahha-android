package sdk.sahha.android.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.device.PhoneUsageSilver
import sdk.sahha.android.source.Sahha

class PhoneScreenStateReceiver : BroadcastReceiver() {
    private val ioScope by lazy { CoroutineScope(IO) }

    override fun onReceive(context: Context, intent: Intent) {
        ioScope.launch {
            SahhaReconfigure(context)
            saveScreenStateData()
        }
    }

    private suspend fun saveScreenStateData() {
        val isLocked = Sahha.di.keyguardManager.isKeyguardLocked
        val isScreenOn = Sahha.di.powerManager.isInteractive
        val nowInIso = Sahha.di.timeManager.nowInISO()

        saveScreenState(isLocked, isScreenOn, nowInIso)
        saveScreenStateForSilverLayer(isLocked, isScreenOn, nowInIso)
    }

    private suspend fun saveScreenState(
        isLocked: Boolean,
        isScreenOn: Boolean,
        nowInIso: String
    ) {
        Sahha.di.sensorRepo.savePhoneUsage(
            PhoneUsage(isLocked, isScreenOn, nowInIso)
        )
    }

    private suspend fun saveScreenStateForSilverLayer(
        isLocked: Boolean,
        isScreenOn: Boolean,
        nowInIso: String
    ) {
        Sahha.di.sensorRepo.savePhoneUsageSilver(
            PhoneUsageSilver(isLocked, isScreenOn, nowInIso)
        )
    }
}
