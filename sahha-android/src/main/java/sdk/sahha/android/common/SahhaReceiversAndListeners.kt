package sdk.sahha.android.common

import android.os.Build
import androidx.annotation.RequiresApi
import sdk.sahha.android.framework.listeners.StepCounterListener
import sdk.sahha.android.framework.listeners.StepDetectorListener
import sdk.sahha.android.framework.receiver.PhoneScreenStateReceiver
import sdk.sahha.android.framework.receiver.SleepReceiver
import sdk.sahha.android.framework.receiver.TimeZoneChangedReceiver

internal object SahhaReceiversAndListeners {
    //Receivers
    val screenLocks = PhoneScreenStateReceiver()
    val timezoneDetector = TimeZoneChangedReceiver()

    @RequiresApi(Build.VERSION_CODES.Q)
    val sleepSegments = SleepReceiver()

    //Listeners
    val stepDetector = StepDetectorListener()
    val stepCounter = StepCounterListener()
}