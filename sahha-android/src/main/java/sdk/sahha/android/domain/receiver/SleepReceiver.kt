package sdk.sahha.android.domain.receiver

import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.SleepSegmentEvent
import com.sahha.android.model.SleepQueue
import com.sahha.android.model.SleepQueueHistory
import kotlinx.coroutines.launch
import sdk.sahha.android.Sahha
import sdk.sahha.android.common.SahhaPermissions
import sdk.sahha.android.data.Constants

class SleepReceiver : BroadcastReceiver() {
    private val timeManager by lazy { Sahha.di.timeManager }
    private val sleepDao by lazy { Sahha.di.sleepDao }
    private val ioScope by lazy { Sahha.di.ioScope }

    private val tag = "SleepReceiver"

    private var start: Long = 0L
    private var end: Long = 0L

    override fun onReceive(context: Context, intent: Intent) {

        // First check activity permissions
        if (!SahhaPermissions.activityRecognitionGranted()) {
            notifyPermissionsIssue(context)
            return
        }

        // Sleep data is found
        checkSleepData(intent)
    }

    private fun notifyPermissionsIssue(context: Context) {
        val notificationManager =
            (context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager)
        notificationManager.cancel(Constants.NOTIFICATION_PERMISSION_SETTINGS)

        Sahha.notifications.notifyWithSettingsIntent(
            "Sleep Settings",
            "Please tap here to re-enable activity recognition permissions."
        )
    }

    private fun checkSleepData(intent: Intent) {
        if (SleepSegmentEvent.hasEvents(intent)) {
            val sleepSegmentEvents = SleepSegmentEvent.extractEvents(intent)

            ioScope.launch {
                for (segment in sleepSegmentEvents) {
                    start = segment.startTimeMillis
                    end = segment.endTimeMillis
                    if (start == 0L && end == 0L) return@launch

                    checkForDuplicate()
                }
            }
        }
    }

    private suspend fun checkForDuplicate() {
        val specificSleepSegment = sleepDao.getSleepHistoryWith(start, end)
        if (specificSleepSegment.isNullOrEmpty()) {
            saveSleep()
            removeExcessSleepHistory(50)
            saveSleepHistory()
        } else {
            sleepDao.removeSleep(specificSleepSegment.first().id)
        }
    }

    private suspend fun saveSleep() {
        sleepDao.saveSleep(
            SleepQueue(
                start,
                end,
                timeManager.epochMillisToISO(end)
            )
        )
    }

    private suspend fun saveSleepHistory() {
        // Redundancy
        try {
            sleepDao.saveSleepHistory(
                SleepQueueHistory(
                    sleepDao.getSleepQueue().last().id,
                    start,
                    end,
                    timeManager.epochMillisToISO(end)
                )
            )
        } catch (e: Exception) {
            Log.w(tag, "Could not save sleep history", e)
        }
    }

    private suspend fun removeExcessSleepHistory(maxCount: Int) {
        try {
            checkAndRemove(maxCount)
        } catch (e: Exception) {
            Log.w(tag, "Failed to remove excess history", e)
        }
    }

    private suspend fun checkAndRemove(
        maxCount: Int
    ) {
        var history = sleepDao.getSleepQueueHistory()

        if (history.count() > maxCount) {
            for (i in history.indices) {
                if (i > maxCount - 1) {
                    sleepDao.removeHistory(history[0].id)
                    history = sleepDao.getSleepQueueHistory()
                }
            }
        }
    }
}