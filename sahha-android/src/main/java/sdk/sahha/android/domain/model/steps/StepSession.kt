package sdk.sahha.android.domain.model.steps

import androidx.health.connect.client.records.metadata.Device
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethodsHealthConnect
import sdk.sahha.android.domain.model.dto.SahhaDataLogDto
import sdk.sahha.android.source.Sahha

@Entity
data class StepSession(
    val count: Int,
    val startDateTime: String,
    val endDateTime: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
)

fun StepSession.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
        logType = Constants.DataLogs.ACTIVITY,
        dataType = Constants.DataTypes.SAHHA_STEP_SESSION,
        value = count.toDouble(),
        unit = Constants.DataUnits.COUNT,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        source = Constants.STEP_DETECTOR_DATA_SOURCE,
        deviceType = Sahha.di.healthConnectConstantsMapper.devices(Device.TYPE_PHONE),
        recordingMethod = RecordingMethodsHealthConnect.RECORDING_METHOD_AUTOMATICALLY_RECORDED.name,
    )
}
