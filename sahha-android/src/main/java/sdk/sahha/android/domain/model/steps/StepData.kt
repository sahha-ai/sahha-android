package sdk.sahha.android.domain.model.steps

import androidx.health.connect.client.records.metadata.Device
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethodsHealthConnect
import sdk.sahha.android.domain.model.dto.SahhaDataLogDto
import sdk.sahha.android.domain.model.dto.StepDto
import sdk.sahha.android.source.Sahha
import java.util.UUID

@Entity
data class StepData(
    val source: String,
    val count: Int,
    val detectedAt: String,
    @PrimaryKey val id: String = UUID.randomUUID().toString()
)

fun StepData.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
        id = id,
        logType = Constants.DataLogs.ACTIVITY,
        dataType = getDataType(source),
        value = count.toDouble(),
        unit = Constants.DataUnits.COUNT,
        source = source,
        startDateTime = detectedAt,
        endDateTime = detectedAt,
        deviceType = Sahha.di.healthConnectConstantsMapper.devices(Device.TYPE_PHONE),
        recordingMethod = RecordingMethodsHealthConnect.RECORDING_METHOD_AUTOMATICALLY_RECORDED.name,
    )
}

private fun getDataType(source: String): String {
    return when (source) {
        Constants.STEP_COUNTER_DATA_SOURCE -> {
            Constants.DataTypes.STEP_COUNTER
        }

        Constants.STEP_DETECTOR_DATA_SOURCE -> {
            Constants.DataTypes.STEP_DETECTOR
        }

        else -> "Unknown"
    }
}
