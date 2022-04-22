package sdk.sahha.android.domain.receiver

import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.location.SleepSegmentEvent
import com.sahha.android.model.SleepQueue
import com.sahha.android.model.SleepQueueHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.common.SahhaPermissions
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.remote.dto.SleepDto

@RequiresApi(Build.VERSION_CODES.Q)
class SleepReceiver : BroadcastReceiver() {
    private val tag = "SleepReceiver"

    private var start: Long = 0L
    private var end: Long = 0L

    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Default).launch {
            SahhaReconfigure(context.applicationContext)

            // First check activity permissions
            if (!SahhaPermissions.activityRecognitionGranted(context)) {
                notifyPermissionsIssue(context)
                return@launch
            }

            // Sleep data is found
            checkSleepData(intent)
        }
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

            Sahha.di.ioScope.launch {
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
        val specificSleepSegment = Sahha.di.sleepDao.getSleepHistoryWith(start, end)
        if (specificSleepSegment.isNullOrEmpty()) {
            saveSleepDto()
            saveSleep()
            saveSleepHistory()
            removeExcessSleepHistory(50)
        } else {
            Sahha.di.sleepDao.removeSleep(specificSleepSegment.first().id)
        }
    }

    private suspend fun saveSleepDto() {
        val millisSlept = end - start
        val minutesSlept = millisSlept / 1000 / 60
        Sahha.di.sleepDao.saveSleepDto(
            SleepDto(
                minutesSlept.toInt(),
                Sahha.timeManager!!.epochMillisToISO(start),
                Sahha.timeManager!!.epochMillisToISO(end),
                Sahha.timeManager!!.nowInISO()
            )
        )
    }

    private suspend fun saveSleep() {
        Sahha.di.sleepDao.saveSleep(
            SleepQueue(
                start,
                end,
                Sahha.timeManager!!.nowInISO()
            )
        )
    }

    private suspend fun saveSleepHistory() {
        // Redundancy
        try {
            Sahha.di.sleepDao.saveSleepHistory(
                SleepQueueHistory(
                    Sahha.di.sleepDao.getSleepQueue().last().id,
                    start,
                    end,
                    Sahha.timeManager!!.nowInISO()
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
        var history = Sahha.di.sleepDao.getSleepQueueHistory()

        if (history.count() > maxCount) {
            for (i in history.indices) {
                if (i > maxCount - 1) {
                    Sahha.di.sleepDao.removeHistory(history[0].id)
                    history = Sahha.di.sleepDao.getSleepQueueHistory()
                }
            }
        }
    }
}