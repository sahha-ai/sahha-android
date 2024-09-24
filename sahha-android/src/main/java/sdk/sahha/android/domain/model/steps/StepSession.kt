package sdk.sahha.android.domain.model.steps

import androidx.health.connect.client.records.metadata.Device
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethods
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.source.Sahha
import java.util.UUID

@Entity
internal data class StepSession(
    val count: Int,
    val startDateTime: String,
    val endDateTime: String,
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
)

internal fun StepSession.toSahhaDataLogAsChildLog(): SahhaDataLog {
    return SahhaDataLog(
        id = id,
        logType = Constants.DataLogs.ACTIVITY,
        dataType = Constants.DataTypes.SAHHA_STEP_SESSION,
        value = count.toDouble(),
        unit = Constants.DataUnits.COUNT,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        source = Constants.STEP_DETECTOR_DATA_SOURCE,
        deviceType = Sahha.di.healthConnectConstantsMapper.devices(Device.TYPE_PHONE),
        recordingMethod = RecordingMethods.AUTOMATICALLY_RECORDED.name,
    )
}
