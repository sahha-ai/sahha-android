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
internal data class StepData(
    val source: String,
    val count: Int,
    val detectedAt: String,
    @PrimaryKey val id: String = UUID.randomUUID().toString()
)

internal fun StepData.toSahhaDataLogAsChildLog(): SahhaDataLog {
    return SahhaDataLog(
        id = id,
        logType = Constants.DataLogs.ACTIVITY,
        dataType = getDataType(source),
        value = count.toDouble(),
        unit = Constants.DataUnits.COUNT,
        source = source,
        startDateTime = detectedAt,
        endDateTime = detectedAt,
        deviceType = Sahha.di.healthConnectConstantsMapper.devices(Device.TYPE_PHONE),
        recordingMethod = RecordingMethods.AUTOMATICALLY_RECORDED.name,
    )
}

private fun getDataType(source: String): String {
    return when (source) {
        Constants.STEP_COUNTER_DATA_SOURCE -> {
            "total_steps"
        }

        Constants.STEP_DETECTOR_DATA_SOURCE -> {
            "single_step"
        }

        else -> "unknown_steps"
    }
}
