package sdk.sahha.android.data.manager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognitionClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReceiversAndListeners
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.receiver.ActivityRecognitionReceiver
import sdk.sahha.android.domain.manager.ReceiverManager

class ReceiverManagerImpl(
    private val context: Context,
    private val mainScope: CoroutineScope
) : ReceiverManager {
    private val activityRecognitionIntent by lazy {
        Intent(
            context,
            ActivityRecognitionReceiver::class.java
        )
    }

    private var activityRecognitionReceiverRegistered = false

    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private lateinit var activityRecognitionPendingIntent: PendingIntent

    override fun startActivityRecognitionReceiver(callback: ((error: String?, success: Boolean) -> Unit)?) {
        if (activityRecognitionReceiverRegistered) return

        mainScope.launch {
            setActivityRecognitionClient()
            setActivityRecognitionPendingIntent()
            requestActivityRecognitionUpdates(callback)
        }
    }

    override fun startPhoneScreenReceivers(
        serviceContext: Context,
    ) {
        registerScreenStateReceiver(serviceContext)
    }

    override fun startTimeZoneChangedReceiver(context: Context) {
        registerTimeZoneChangedReceiver(context)
    }

    private fun setActivityRecognitionClient() {
        activityRecognitionClient = ActivityRecognitionClient(context)
    }

    private fun setActivityRecognitionPendingIntent() {
        activityRecognitionPendingIntent = getPendingIntentWithMutableFlag()
    }

    private fun requestActivityRecognitionUpdates(callback: ((error: String?, success: Boolean) -> Unit)?) {
        activityRecognitionClient.requestActivityUpdates(
            Constants.ACTIVITY_RECOGNITION_UPDATE_INTERVAL_MILLIS,
            activityRecognitionPendingIntent
        ).addOnSuccessListener {
            activityRecognitionReceiverRegistered = true
            callback?.also { it(null, true) }
        }.addOnFailureListener { e ->
            callback?.also { it(e.message, false) }
            displayErrorToast(e)
        }
    }

    private fun displayErrorToast(e: Exception) {
        Toast.makeText(
            context,
            "Activity recognition request failed: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun getPendingIntentWithMutableFlag(): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            Constants.ACTIVITY_RECOGNITION_RECEIVER,
            activityRecognitionIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    private fun registerScreenStateReceiver(serviceContext: Context) {
        ContextCompat.registerReceiver(
            serviceContext.applicationContext,
            SahhaReceiversAndListeners.screenLocks,
            IntentFilter().apply {
                addAction(Intent.ACTION_USER_PRESENT)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            },
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun registerTimeZoneChangedReceiver(context: Context) {
        ContextCompat.registerReceiver(
            context.applicationContext,
            SahhaReceiversAndListeners.timezoneDetector,
            IntentFilter().apply {
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            },
            ContextCompat.RECEIVER_EXPORTED
        )
    }
}