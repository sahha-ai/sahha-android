package sdk.sahha.android.framework.manager

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.SleepSegmentRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.Constants.SLEEP_DATA_REQUEST
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaReceiversAndListeners
import sdk.sahha.android.domain.manager.ReceiverManager
import sdk.sahha.android.framework.receiver.ActivityRecognitionReceiver
import sdk.sahha.android.framework.receiver.SleepReceiver

private const val TAG = "ReceiverManagerImpl"

internal class ReceiverManagerImpl(
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
            setActivityRecognitionPendingIntent(callback = callback)
            requestActivityRecognitionUpdates(callback)
        }
    }

    override fun startPhoneScreenReceivers(
        serviceContext: Context,
    ) {
        registerScreenStateReceiver(serviceContext)
    }

    override fun startSleepReceiver(
        serviceContext: Context
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return

        registerSleepReceiver(serviceContext)
    }

    override fun startTimeZoneChangedReceiver(context: Context) {
        registerTimeZoneChangedReceiver(context)
    }

    private fun setActivityRecognitionClient() {
        activityRecognitionClient = ActivityRecognition.getClient(context)
    }

    private fun setActivityRecognitionPendingIntent(callback: ((error: String?, success: Boolean) -> Unit)?) {
        val isAndroid12AndAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val isBelowAndroid8 = Build.VERSION.SDK_INT < 26
        activityRecognitionPendingIntent =
            if (isBelowAndroid8) {
                callback?.also { it(SahhaErrors.androidVersionTooLow(8), false) }
                return
            } else {
                getPendingIntentWithMutableFlag()
            }
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
        serviceContext.registerReceiver(
            SahhaReceiversAndListeners.screenLocks,
            IntentFilter().apply {
                addAction(Intent.ACTION_USER_PRESENT)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            }
        )
    }

    private fun registerSleepReceiver(serviceContext: Context) {
        val sleepIntent = Intent(serviceContext, SleepReceiver::class.java)
        val sleepPendingIntent = PendingIntent.getBroadcast(
            serviceContext,
            SLEEP_DATA_REQUEST,
            sleepIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        if (ActivityCompat.checkSelfPermission(
                serviceContext,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            ActivityRecognition.getClient(context).requestSleepSegmentUpdates(
                sleepPendingIntent,
                SleepSegmentRequest.getDefaultSleepSegmentRequest()
            ).addOnSuccessListener { Log.d(TAG, "Successful sleep segment request") }
                .addOnFailureListener { Log.d(TAG, "Unsuccessful sleep segment request") }
        }
    }

    private fun registerTimeZoneChangedReceiver(context: Context) {
        context.registerReceiver(
            SahhaReceiversAndListeners.timezoneDetector,
            IntentFilter().apply {
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            }
        )
    }
}