package sdk.sahha.android.domain.model.steps

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.model.dto.StepDto

@Entity
data class StepData(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val source: String,
    val count: Int,
    val detectedAt: String
) {
    constructor(
        source: String,
        count: Int,
        detectedAt: String
    ) : this(
        0,
        source,
        count,
        detectedAt
    )
}

fun StepData.toStepDto(createdAt: String): StepDto {
    return StepDto(
        getDataType(source),
        count,
        source,
        false,
        detectedAt,
        detectedAt,
        createdAt
    )
}

private fun getDataType(source: String): String {
    return when (source) {
        Constants.STEP_COUNTER_DATA_SOURCE -> {
            Constants.STEP_COUNTER_DATA_TYPE
        }
        Constants.STEP_DETECTOR_DATA_SOURCE -> {
            Constants.STEP_DETECTOR_DATA_TYPE
        }
        else -> "Unknown"
    }
}
