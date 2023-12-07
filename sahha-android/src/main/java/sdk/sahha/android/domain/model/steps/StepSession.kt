package sdk.sahha.android.domain.model.steps

import android.os.Build
import androidx.health.connect.client.records.metadata.Device
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethodsHealthConnect
import sdk.sahha.android.domain.mapper.HealthConnectConstantsMapper
import sdk.sahha.android.domain.model.dto.StepDto
import sdk.sahha.android.framework.mapper.HealthConnectConstantsMapperImpl
import sdk.sahha.android.source.Sahha

@Entity
data class StepSession(
    val count: Int,
    val startDateTime: String,
    val endDateTime: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
)

fun StepSession.toStepDto(): StepDto {
    return StepDto(
        value = count,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        dataType = Constants.DataTypes.SAHHA_STEP_SESSION,
        source = Constants.STEP_DETECTOR_DATA_SOURCE,
        deviceType = Sahha.di.healthConnectConstantsMapper.devices(Device.TYPE_PHONE),
        modifiedDateTime = endDateTime,
        recordingMethod = RecordingMethodsHealthConnect.RECORDING_METHOD_AUTOMATICALLY_RECORDED.name,
        deviceManufacturer = Build.MANUFACTURER,
        deviceModel = Build.MODEL
    )
}
