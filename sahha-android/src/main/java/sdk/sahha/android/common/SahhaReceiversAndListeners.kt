package sdk.sahha.android.common

import sdk.sahha.android.data.listeners.StepCounterListener
import sdk.sahha.android.data.listeners.StepDetectorListener
import sdk.sahha.android.data.receiver.PhoneScreenStateReceiver
import sdk.sahha.android.data.receiver.TimeZoneChangedReceiver

object SahhaReceiversAndListeners {
    //Receivers
    val screenLocks = PhoneScreenStateReceiver()
    val timezoneDetector = TimeZoneChangedReceiver()

    //Listeners
    val stepDetector = StepDetectorListener()
    val stepCounter = StepCounterListener()
}