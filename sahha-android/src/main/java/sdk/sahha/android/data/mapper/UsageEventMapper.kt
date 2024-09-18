package sdk.sahha.android.data.mapper

import android.app.usage.UsageEvents
import androidx.health.connect.client.records.metadata.Device
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethodsHealthConnect
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.source.Sahha
import java.util.UUID

private val timeManager = Sahha.di.timeManager
private val mapper = Sahha.di.healthConnectConstantsMapper
internal fun UsageEvents.Event.toSahhaDataLog(): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        logType = Constants.DataLogs.DEVICE,
        dataType = Constants.DataTypes.APP_USAGE,
        value = 0.0,
        source = this.packageName ?: "UNKNOWN",
        startDateTime = timeManager.epochMillisToISO(this.timeStamp),
        endDateTime = timeManager.epochMillisToISO(this.timeStamp),
        unit = Constants.DataUnits.MILLISECOND,
        recordingMethod = RecordingMethodsHealthConnect.AUTOMATICALLY_RECORDED.name,
        deviceType = mapper.devices(Device.TYPE_PHONE),
        additionalProperties = hashMapOf(
            "eventType" to UsageEventMapper.getString(this.eventType)
        )
    )
}

object UsageEventMapper {
    fun getString(num: Int): String {
        return when (num) {
            UsageEvents.Event.ACTIVITY_RESUMED -> "ACTIVITY_RESUMED"
            UsageEvents.Event.ACTIVITY_PAUSED -> "ACTIVITY_PAUSED"
            UsageEvents.Event.ACTIVITY_STOPPED -> "ACTIVITY_STOPPED"
            UsageEvents.Event.DEVICE_SHUTDOWN -> "DEVICE_SHUTDOWN"
            UsageEvents.Event.DEVICE_STARTUP -> "DEVICE_STARTUP"
            UsageEvents.Event.CONFIGURATION_CHANGE -> "CONFIGURATION_CHANGE"
            UsageEvents.Event.FOREGROUND_SERVICE_START -> "FOREGROUND_SERVICE_START"
            UsageEvents.Event.FOREGROUND_SERVICE_STOP -> "FOREGROUND_SERVICE_STOP"
            UsageEvents.Event.KEYGUARD_HIDDEN -> "KEYGUARD_HIDDEN"
            UsageEvents.Event.KEYGUARD_SHOWN -> "KEYGUARD_SHOWN"
            UsageEvents.Event.SCREEN_INTERACTIVE -> "SCREEN_INTERACTIVE"
            UsageEvents.Event.SCREEN_NON_INTERACTIVE -> "SCREEN_NON_INTERACTIVE"
            UsageEvents.Event.SHORTCUT_INVOCATION -> "SHORTCUT_INVOCATION"
            UsageEvents.Event.STANDBY_BUCKET_CHANGED -> "STANDBY_BUCKET_CHANGED"
            UsageEvents.Event.USER_INTERACTION -> "USER_INTERACTION"
            UsageEvents.Event.NONE -> "NONE"
            UsageEvents.Event.MOVE_TO_BACKGROUND -> "MOVE_TO_BACKGROUND"
            UsageEvents.Event.MOVE_TO_FOREGROUND -> "MOVE_TO_FOREGROUND"
            else -> Constants.UNKNOWN
        }
    }
}