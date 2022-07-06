package sdk.sahha.android.common

import android.os.Build
import androidx.annotation.RequiresApi
import sdk.sahha.android.domain.receiver.PhoneScreenStateReceiver

object SahhaReceivers {

    @RequiresApi(Build.VERSION_CODES.O)
    val screenLocks = PhoneScreenStateReceiver()
}