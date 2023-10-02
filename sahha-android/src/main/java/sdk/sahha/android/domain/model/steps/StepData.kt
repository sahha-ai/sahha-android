package sdk.sahha.android.domain.model.steps

import androidx.health.connect.client.records.metadata.Device
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.model.dto.StepDto
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

fun StepData.toStepDto(): StepDto {
    return StepDto(
        dataType = getDataType(source),
        count = count,
        source = source,
        startDateTime = detectedAt,
        endDateTime = detectedAt,
        modifiedDateTime = detectedAt,
        sourceDevice = Sahha.di.healthConnectConstantsMapper.devices(Device.TYPE_PHONE)
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
