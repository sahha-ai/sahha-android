package sdk.sahha.android.framework.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.manager.SahhaAlarmManager
import sdk.sahha.android.framework.receiver.HealthConnectQueryReceiver
import sdk.sahha.android.framework.receiver.InsightsQueryReceiver
import javax.inject.Inject

class SahhaAlarmManagerImpl @Inject constructor(
    private val alarmManager: AlarmManager
) : SahhaAlarmManager {
    override fun getHealthConnectQueryPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, HealthConnectQueryReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            Constants.HEALTH_CONNECT_QUERY_RECEIVER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getInsightsQueryPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, InsightsQueryReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            Constants.INSIGHTS_QUERY_RECEIVER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun setAlarm(pendingIntent: PendingIntent, setTimeEpochMillis: Long) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            setTimeEpochMillis,
            pendingIntent
        )
    }

    override fun stopAlarm(pendingIntent: PendingIntent) {
        alarmManager.cancel(pendingIntent)
    }

    override fun stopAllAlarms(context: Context) {
        val hcQueryPendingIntent = getHealthConnectQueryPendingIntent(context)
        val insightsQueryPendingIntent = getInsightsQueryPendingIntent(context)

        alarmManager.cancel(hcQueryPendingIntent)
        alarmManager.cancel(insightsQueryPendingIntent)
    }
}