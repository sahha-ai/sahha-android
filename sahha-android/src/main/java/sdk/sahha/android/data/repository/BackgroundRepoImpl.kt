package sdk.sahha.android.data.repository

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
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
import sdk.sahha.android.common.SahhaReceiversAndListeners
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.Constants.ACTIVITY_RECOGNITION_UPDATE_INTERVAL_MILLIS
import sdk.sahha.android.data.Constants.DEVICE_POST_WORKER_TAG
import sdk.sahha.android.data.Constants.SLEEP_POST_WORKER_TAG
import sdk.sahha.android.data.Constants.STEP_POST_WORKER_TAG
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.data.local.dao.DeviceUsageDao
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.data.local.dao.SleepDao
import sdk.sahha.android.domain.model.config.SahhaNotificationConfiguration
import sdk.sahha.android.domain.receiver.ActivityRecognitionReceiver
import sdk.sahha.android.domain.repository.BackgroundRepo
import sdk.sahha.android.domain.service.DataCollectionService
import sdk.sahha.android.domain.worker.SleepCollectionWorker
import sdk.sahha.android.domain.worker.post.DevicePostWorker
import sdk.sahha.android.domain.worker.post.SleepPostWorker
import sdk.sahha.android.domain.worker.post.StepPostWorker
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import java.util.concurrent.TimeUnit

@SuppressLint("NewApi")
class BackgroundRepoImpl(
    private val context: Context,
    private val defaultScope: CoroutineScope,
    private val ioScope: CoroutineScope,
    private val configDao: ConfigurationDao,
    private val deviceDao: DeviceUsageDao,
    private val sleepDao: SleepDao,
    private val movementDao: MovementDao
) : BackgroundRepo {
    override lateinit var notification: Notification

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

            val notificationConfig = Sahha.di.configurationDao.getNotificationConfig()
            Sahha.notifications.setNewPersistent(
                notificationConfig.icon,
                notificationConfig.title,
                notificationConfig.shortDescription
            )

            try {
                context.startForegroundService(
                    Intent(context, DataCollectionService::class.java)
                        .setAction(Constants.ACTION_RESTART_SERVICE)
                )
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
    ) {
        if (Build.VERSION.SDK_INT < 26) return

        registerScreenStateReceiver(serviceContext)
    }

    override suspend fun startStepDetectorAsync(
        context: Context,
        movementDao: MovementDao,
    ) {
        val sensorManager = Sahha.di.sensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        sensor?.also {
            sensorManager.registerListener(
                SahhaReceiversAndListeners.stepDetector, it, SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override suspend fun startStepCounterAsync(
        context: Context,
        movementDao: MovementDao,
    ) {
        val sensorManager = Sahha.di.sensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        sensor?.also {
            sensorManager.registerListener(
                SahhaReceiversAndListeners.stepCounter,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun startSleepWorker(repeatIntervalMinutes: Long, workerTag: String) {
        val checkedIntervalMinutes = getCheckedIntervalMinutes(repeatIntervalMinutes)
        val workRequest: PeriodicWorkRequest =
            getSleepWorkRequest(checkedIntervalMinutes, workerTag)
        startWorkManager(workRequest, workerTag, ExistingPeriodicWorkPolicy.REPLACE)
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
                startStepPostWorker(15, STEP_POST_WORKER_TAG)
            }
        }
    }

    override fun stopWorkerByTag(workerTag: String) {
        workManager.cancelAllWorkByTag(workerTag)
    }

    override fun stopAllWorkers() {
        workManager.cancelAllWork()
    }

    override suspend fun getSensorData(
        sensor: SahhaSensor,
        callback: ((error: String?, successful: String?) -> Unit)
    ) {
        try {
            when (sensor) {
                SahhaSensor.device -> {
                    getDeviceDataSummary()?.also {
                        callback(null, it)
                        return
                    }
                }
                SahhaSensor.sleep -> {
                    getSleepDataSummary()?.also {
                        callback(null, it)
                        return
                    }
                }
                SahhaSensor.pedometer -> {
                    getStepDataSummary()?.also {
                        callback(null, it)
                        return
                    }
                }
            }
            callback("No data found", null)
        } catch (e: Exception) {
            callback("Error: " + e.message, null)
        }
    }

    private suspend fun getDeviceDataSummary(): String? {
        var dataSummary = ""
        deviceDao.getUsages().forEach {
            dataSummary += "Locked: ${it.isLocked}\nScreen on: ${it.isScreenOn}\nAt: ${it.createdAt}\n\n"
        }
        return dataSummary
    }

    private suspend fun getStepDataSummary(): String? {
        var dataSummary = ""
        movementDao.getAllStepData().forEach {
            if (it.source == Constants.STEP_DETECTOR_DATA_SOURCE)
                dataSummary += "${it.count} step\nAt: ${it.detectedAt}\n\n"
            if (it.source == Constants.STEP_COUNTER_DATA_SOURCE)
                dataSummary += "${it.count} total steps since last phone boot\nAt: ${it.detectedAt}\n\n"
        }
        return dataSummary
    }

    private suspend fun getSleepDataSummary(): String? {
        var dataSummary: String? = null
        sleepDao.getSleepDto().forEach {
            dataSummary += "Slept: ${it.durationInMinutes} minutes\nFrom: ${it.startDateTime}\nTo: ${it.endDateTime}\n\n"
        }
        return dataSummary
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    private fun startStepPostWorker(repeatIntervalMinutes: Long, workerTag: String) {
        val checkedIntervalMinutes = getCheckedIntervalMinutes(repeatIntervalMinutes)
        val workRequest = getStepPostWorkRequest(checkedIntervalMinutes, workerTag)
        startWorkManager(workRequest, workerTag)
    }

    // Force default minimum value of 15 minutes
    private fun getCheckedIntervalMinutes(interval: Long): Long {
        return if (interval < 15) 15 else interval
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

    private fun getStepPostWorkRequest(
        repeatIntervalMinutes: Long,
        workerTag: String
    ): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<StepPostWorker>(
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

    private fun startWorkManager(
        workRequest: PeriodicWorkRequest,
        workerTag: String,
        policy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP
    ) {
        workManager.enqueueUniquePeriodicWork(
            workerTag,
            policy,
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