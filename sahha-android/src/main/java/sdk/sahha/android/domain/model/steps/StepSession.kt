package sdk.sahha.android.domain.model.steps

import androidx.health.connect.client.records.metadata.Device
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethodsHealthConnect
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.metadata.HasMetadata
import sdk.sahha.android.domain.model.metadata.SahhaMetadata
import sdk.sahha.android.source.Sahha
import java.util.UUID

@Entity
internal data class StepSession(
    val count: Int,
    val startDateTime: String,
    val endDateTime: String,
    override val metadata: SahhaMetadata? = null,
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
): HasMetadata<StepSession> {
    override fun copyWithMetadata(metadata: SahhaMetadata): StepSession {
        return this.copy(metadata = metadata)
    }
}

internal fun StepSession.toSahhaDataLogAsChildLog(): SahhaDataLog {
    return SahhaDataLog(
        id = id,
        logType = Constants.DataLogs.ACTIVITY,
        dataType = Constants.DataTypes.SAHHA_STEP_SESSION,
        value = count.toDouble(),
        unit = Constants.DataUnits.COUNT,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        source = Constants.STEP_DETECTOR_DATA_SOURCE,
        deviceType = Sahha.di.healthConnectConstantsMapper.devices(Device.TYPE_PHONE),
        recordingMethod = RecordingMethodsHealthConnect.AUTOMATICALLY_RECORDED.name,
        metadata = metadata
    )
}
