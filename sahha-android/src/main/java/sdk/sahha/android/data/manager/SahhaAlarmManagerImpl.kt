package sdk.sahha.android.data.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.receiver.SahhaAlarmReceiver
import sdk.sahha.android.domain.manager.SahhaAlarmManager
import javax.inject.Inject

class SahhaAlarmManagerImpl @Inject constructor(
    private val alarmManager: AlarmManager
) : SahhaAlarmManager {
    override fun setAlarm(context: Context, setTimeEpochMillis: Long) {
        val alarmIntent = Intent(context, SahhaAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            Constants.SAHHA_ALARM_RECEIVER,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.set(AlarmManager.RTC_WAKEUP, setTimeEpochMillis, pendingIntent)
    }
}