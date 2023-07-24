package sdk.sahha.android.domain.model.steps

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.model.dto.StepDto

@Entity
data class StepSession(
    val count: Int,
    val startDateTime: String,
    val endDateTime: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
)

fun StepSession.toStepDto(
    dataType: String = Constants.CUSTOM_STEP_SESSION_DATA_TYPE,
    source: String = Constants.STEP_DETECTOR_DATA_SOURCE
): StepDto {
    return StepDto(
        count = count,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        dataType = dataType,
        source = source,
        manuallyEntered = false
    )
}
