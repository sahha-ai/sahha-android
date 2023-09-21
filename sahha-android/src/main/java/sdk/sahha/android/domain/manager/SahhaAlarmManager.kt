package sdk.sahha.android.domain.manager

import android.content.Context

interface SahhaAlarmManager {
    fun setAlarm(context: Context, setTimeEpochMillis: Long)
}