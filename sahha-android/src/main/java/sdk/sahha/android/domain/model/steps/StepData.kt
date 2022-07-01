package sdk.sahha.android.domain.model.steps

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.data.remote.dto.StepDto
import sdk.sahha.android.source.Sahha

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
        StepDataSource.AndroidStepCounter.name -> {
            "TotalSteps"
        }
        StepDataSource.AndroidStepDetector.name -> {
            "SingleStep"
        }
        else -> "Unknown"
    }
}
