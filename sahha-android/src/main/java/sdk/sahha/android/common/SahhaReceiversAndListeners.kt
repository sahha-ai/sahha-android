package sdk.sahha.android.common

import android.os.Build
import androidx.annotation.RequiresApi
import sdk.sahha.android.data.listeners.StepCounterListener
import sdk.sahha.android.data.listeners.StepDetectorListener
import sdk.sahha.android.domain.receiver.PhoneScreenStateReceiver

object SahhaReceiversAndListeners {
    //Receivers
    @RequiresApi(Build.VERSION_CODES.O)
    val screenLocks = PhoneScreenStateReceiver()

    //Listeners
    val stepDetector = StepDetectorListener()
    val stepCounter = StepCounterListener()
}