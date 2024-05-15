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
        source = this.packageName,
        startDateTime = timeManager.epochMillisToISO(this.timeStamp),
        endDateTime = timeManager.epochMillisToISO(this.timeStamp),
        unit = Constants.DataUnits.MILLISECOND,
        recordingMethod = RecordingMethodsHealthConnect.RECORDING_METHOD_AUTOMATICALLY_RECORDED.name,
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
            UsageEvents.Event.DEVICE_SHUTDOWN -> "DEVICE_SHUTDOWN"
            UsageEvents.Event.DEVICE_STARTUP -> "DEVICE_STARTUP"
            else -> Constants.UNKNOWN
        }
    }
}