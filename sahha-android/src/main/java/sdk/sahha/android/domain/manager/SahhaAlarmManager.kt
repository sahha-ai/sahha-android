package sdk.sahha.android.domain.manager

import android.app.PendingIntent

interface SahhaAlarmManager {
    val pendingIntent: PendingIntent
    fun setAlarm(setTimeEpochMillis: Long)
    fun stopAlarm(pendingIntent: PendingIntent)
}