package sdk.sahha.android.data.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import sdk.sahha.android.data.Constants
import sdk.sahha.android.framework.receiver.SahhaAlarmReceiver
import sdk.sahha.android.domain.manager.SahhaAlarmManager
import javax.inject.Inject

class SahhaAlarmManagerImpl @Inject constructor(
    context: Context,
    private val alarmManager: AlarmManager
) : SahhaAlarmManager {
    private val alarmIntent = Intent(context, SahhaAlarmReceiver::class.java)
    override val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
        context,
        Constants.SAHHA_ALARM_RECEIVER,
        alarmIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    override fun setAlarm(setTimeEpochMillis: Long) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, setTimeEpochMillis, pendingIntent)
    }

    override fun stopAlarm(pendingIntent: PendingIntent) {
        alarmManager.cancel(pendingIntent)
    }
}