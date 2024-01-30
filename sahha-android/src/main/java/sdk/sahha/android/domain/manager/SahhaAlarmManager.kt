package sdk.sahha.android.domain.manager

import android.app.PendingIntent
import android.content.Context

internal interface SahhaAlarmManager {
    fun setAlarm(pendingIntent: PendingIntent, setTimeEpochMillis: Long)
    fun stopAlarm(pendingIntent: PendingIntent)
    fun stopAllAlarms(context: Context)
    fun getHealthConnectQueryPendingIntent(context: Context): PendingIntent
    fun getInsightsQueryPendingIntent(context: Context): PendingIntent
}