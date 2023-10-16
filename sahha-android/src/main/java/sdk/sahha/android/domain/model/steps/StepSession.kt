package sdk.sahha.android.domain.model.steps

import androidx.health.connect.client.records.metadata.Device
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.model.dto.StepDto
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
        count = count,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        dataType = Constants.CUSTOM_STEP_SESSION_DATA_TYPE,
        source = Constants.STEP_DETECTOR_DATA_SOURCE,
        deviceType = Sahha.di.healthConnectConstantsMapper.devices(Device.TYPE_PHONE),
        modifiedDateTime = endDateTime
    )
}
