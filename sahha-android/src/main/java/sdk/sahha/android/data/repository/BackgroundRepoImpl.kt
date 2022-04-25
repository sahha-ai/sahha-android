package sdk.sahha.android.data.repository

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.ActivityRecognitionClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.R
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.Constants.ACTIVITY_RECOGNITION_UPDATE_INTERVAL_MILLIS
import sdk.sahha.android.data.Constants.DEVICE_POST_WORKER_TAG
import sdk.sahha.android.data.Constants.SLEEP_POST_WORKER_TAG
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.domain.model.config.SahhaNotificationConfiguration
import sdk.sahha.android.domain.receiver.ActivityRecognitionReceiver
import sdk.sahha.android.domain.receiver.PhoneScreenOffReceiver
import sdk.sahha.android.domain.receiver.PhoneScreenUnlockedReceiver
import sdk.sahha.android.domain.repository.BackgroundRepo
import sdk.sahha.android.domain.service.DataCollectionService
import sdk.sahha.android.domain.worker.SleepCollectionWorker
import sdk.sahha.android.domain.worker.StepWorker
import sdk.sahha.android.domain.worker.post.DevicePostWorker
import sdk.sahha.android.domain.worker.post.SleepPostWorker
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

@SuppressLint("NewApi")
class BackgroundRepoImpl @Inject constructor(
    private val context: Context,
    @Named("defaultScope") private val defaultScope: CoroutineScope,
    @Named("ioScope") private val ioScope: CoroutineScope,
    private val configDao: ConfigurationDao,
    private val api: SahhaApi
) : BackgroundRepo {
    override lateinit var notification: Notification

    private val tag by lazy { "BackgroundRepoImpl" }
    private val workManager by lazy { WorkManager.getInstance(context) }

    private val activityRecognitionIntent by lazy {
        Intent(
            context,
            ActivityRecognitionReceiver::class.java
        )
    }

    private var activityRecognitionReceiverRegistered = false

    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private lateinit var activityRecognitionPendingIntent: PendingIntent

    override fun setSahhaNotification(_notification: Notification) {
        notification = _notification
    }

    override fun startDataCollectionService(
        _icon: Int?,
        _title: String?,
        _shortDescription: String?,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        Sahha.di.defaultScope.launch {
            if (Build.VERSION.SDK_INT < 26) {
                callback?.also { it(SahhaErrors.androidVersionTooLow(8), false) }
                return@launch
            }

            val icon = _icon ?: R.drawable.ic_sahha_no_bg
            val title = _title ?: "Analytics are running"
            val shortDescription =
                _shortDescription ?: "Swipe for options to hide this notification."

            Sahha.di.configurationDao.saveNotificationConfig(
                SahhaNotificationConfiguration(
                    icon,
                    title,
                    shortDescription
                )
            )
            Sahha.notifications.setNewPersistent(icon, title, shortDescription)

            try {
                context.startForegroundService(Intent(context, DataCollectionService::class.java))
            } catch (e: Exception) {
                callback?.also { it(e.message, false) }
            }
        }
    }

    override fun startActivityRecognitionReceiver(callback: ((error: String?, success: Boolean) -> Unit)?) {
        if (activityRecognitionReceiverRegistered) return

        defaultScope.launch {
            setActivityRecognitionClient()
            setActivityRecognitionPendingIntent(callback = callback)
            requestActivityRecognitionUpdates(callback)
        }
    }

    override fun startPhoneScreenReceivers(
        serviceContext: Context,
        receiverRegistered: Boolean,
    ): Boolean {
        if (receiverRegistered) return true
        if (Build.VERSION.SDK_INT < 26) return false

        registerScreenUnlockedReceiver(serviceContext)
        registerScreenOffReceiver(serviceContext)
        return true
    }

    override fun startStepWorker(repeatIntervalMinutes: Long, workerTag: String) {
        val checkedIntervalMinutes = getCheckedIntervalMinutes(repeatIntervalMinutes)
        val workRequest: PeriodicWorkRequest =
            getStepWorkRequest(checkedIntervalMinutes, workerTag)
        startWorkManager(workRequest, workerTag)
    }

    override fun startSleepWorker(repeatIntervalMinutes: Long, workerTag: String) {
        val checkedIntervalMinutes = getCheckedIntervalMinutes(repeatIntervalMinutes)
        val workRequest: PeriodicWorkRequest =
            getSleepWorkRequest(checkedIntervalMinutes, workerTag)
        startWorkManager(workRequest, workerTag)
    }

    override fun startPostWorkersAsync() {
        ioScope.launch {
            val config = configDao.getConfig()
            if (config.sensorArray.contains(SahhaSensor.sleep.ordinal)) {
                startSleepPostWorker(360, SLEEP_POST_WORKER_TAG)
            }

            if (config.sensorArray.contains(SahhaSensor.device.ordinal)) {
                startDevicePostWorker(360, DEVICE_POST_WORKER_TAG)
            }

            if (config.sensorArray.contains(SahhaSensor.pedometer.ordinal)) {
                startPedometerPostWorker()
            }
        }
    }

    override fun stopWorkerByTag(workerTag: String) {
        workManager.cancelAllWorkByTag(workerTag)
    }

    override fun stopAllWorkers() {
        workManager.cancelAllWork()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun registerScreenUnlockedReceiver(serviceContext: Context) {
        serviceContext.registerReceiver(
            PhoneScreenUnlockedReceiver(),
            IntentFilter().apply {
                addAction(Intent.ACTION_USER_PRESENT)
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun registerScreenOffReceiver(serviceContext: Context) {
        serviceContext.registerReceiver(
            PhoneScreenOffReceiver(),
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
            }
        )
    }

    private fun startSleepPostWorker(repeatIntervalMinutes: Long, workerTag: String) {
        val checkedIntervalMinutes = getCheckedIntervalMinutes(repeatIntervalMinutes)
        val workRequest = getSleepPostWorkRequest(checkedIntervalMinutes, workerTag)
        startWorkManager(workRequest, workerTag)
    }

    private fun startDevicePostWorker(repeatIntervalMinutes: Long, workerTag: String) {
        val checkedIntervalMinutes = getCheckedIntervalMinutes(repeatIntervalMinutes)
        val workRequest = getDevicePostWorkRequest(checkedIntervalMinutes, workerTag)
        startWorkManager(workRequest, workerTag)
    }

    private fun startPedometerPostWorker() {}

    // Force default minimum value of 15 minutes
    private fun getCheckedIntervalMinutes(interval: Long): Long {
        return if (interval < 15) 15 else interval
    }

    private fun getStepWorkRequest(
        repeatIntervalMinutes: Long,
        workerTag: String
    ): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<StepWorker>(repeatIntervalMinutes, TimeUnit.MINUTES)
            .addTag(workerTag)
            .build()
    }

    private fun getSleepPostWorkRequest(
        repeatIntervalMinutes: Long,
        workerTag: String
    ): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<SleepPostWorker>(
            repeatIntervalMinutes,
            TimeUnit.MINUTES
        )
            .addTag(workerTag)
            .build()
    }

    private fun getDevicePostWorkRequest(
        repeatIntervalMinutes: Long,
        workerTag: String
    ): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<DevicePostWorker>(
            repeatIntervalMinutes,
            TimeUnit.MINUTES
        )
            .addTag(workerTag)
            .build()
    }

    private fun getSleepWorkRequest(
        repeatIntervalMinutes: Long,
        workerTag: String
    ): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<SleepCollectionWorker>(
            repeatIntervalMinutes,
            TimeUnit.MINUTES
        )
            .addTag(workerTag)
            .build()
    }

    private fun startWorkManager(workRequest: PeriodicWorkRequest, workerTag: String) {
        workManager.enqueueUniquePeriodicWork(
            workerTag,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }


    private fun setActivityRecognitionClient() {
        activityRecognitionClient = ActivityRecognitionClient(context)
    }

    private fun setActivityRecognitionPendingIntent(callback: ((error: String?, success: Boolean) -> Unit)?) {
        val isAndroid12AndAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val isBelowAndroid8 = Build.VERSION.SDK_INT < 26
        activityRecognitionPendingIntent =
            if (isBelowAndroid8) {
                callback?.also { it(SahhaErrors.androidVersionTooLow(8), false) }
                return
            } else if (isAndroid12AndAbove) {
                getPendingIntentWithMutableFlag()
            } else {
                getPendingIntent()
            }
    }

    private fun requestActivityRecognitionUpdates(callback: ((error: String?, success: Boolean) -> Unit)?) {
        activityRecognitionClient.requestActivityUpdates(
            ACTIVITY_RECOGNITION_UPDATE_INTERVAL_MILLIS,
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

    private fun getPendingIntent(): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            Constants.ACTIVITY_RECOGNITION_RECEIVER,
            activityRecognitionIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }
}