package sdk.sahha.android.domain.model.steps

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.domain.model.dto.StepDto

@Entity
data class StepsHealthConnect(
    @PrimaryKey val metaId: String,
    val dataType: String,
    val count: Int,
    val source: String,
    val startDateTime: String,
    val endDateTime: String,
    val modifiedDateTime: String,
    val recordingMethod: String,
    val sourceDevice: String
)

fun StepsHealthConnect.toStepDto(): StepDto {
    return StepDto(
        dataType = dataType,
        recordingMethod = recordingMethod,
        count = count,
        source = source,
        sourceDevice = sourceDevice,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        modifiedDateTime = modifiedDateTime
    )
}