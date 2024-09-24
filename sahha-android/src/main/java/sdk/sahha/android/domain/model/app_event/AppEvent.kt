package sdk.sahha.android.domain.model.app_event

import android.content.Context
import androidx.health.connect.client.records.metadata.Device
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethods
import sdk.sahha.android.domain.mapper.HealthConnectConstantsMapper
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import java.util.UUID

internal data class AppEvent(
    val event: String,
    val dateTime: String,
)

internal fun AppEvent.toSahhaDataLog(
    context: Context,
    mapper: HealthConnectConstantsMapper
): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        logType = Constants.DataLogs.DEVICE,
        dataType = Constants.DataTypes.APP_EVENT,
        source = context.packageName,
        value = 0.0,
        unit = Constants.DataUnits.EMPTY_STRING,
        startDateTime = dateTime,
        endDateTime = dateTime,
        recordingMethod = RecordingMethods.AUTOMATICALLY_RECORDED.name,
        deviceType = mapper.devices(Device.TYPE_PHONE),
    )
}
