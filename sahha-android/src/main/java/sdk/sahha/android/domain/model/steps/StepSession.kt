package sdk.sahha.android.domain.model.steps

import androidx.health.connect.client.records.metadata.Device
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethods
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
    override val postDateTimes: ArrayList<String>? = null,
    override val modifiedDateTime: String? = null,
    @PrimaryKey val id: String = UUID.nameUUIDFromBytes(
            (startTime.toEpochMilli() + endTime.toEpochMilli())
                .toString()
                .toByteArray()
        ).toString(),
): HasMetadata<StepSession> {
    override fun copyWithMetadata(
        postDateTimes: ArrayList<String>?,
        modifiedDateTime: String?,
    ): StepSession {
        return this.copy(
            postDateTimes = postDateTimes,
            modifiedDateTime = modifiedDateTime,
        )
    }
}

internal fun StepSession.toSahhaDataLogAsChildLog(): SahhaDataLog {
    return SahhaDataLog(
        id = id,
        logType = Constants.DataLogs.ACTIVITY,
        dataType = "custom_step_sessions",
        value = count.toDouble(),
        unit = Constants.DataUnits.COUNT,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        source = Constants.STEP_DETECTOR_DATA_SOURCE,
        deviceType = Sahha.di.healthConnectConstantsMapper.devices(Device.TYPE_PHONE),
        recordingMethod = RecordingMethods.AUTOMATICALLY_RECORDED.name,
        postDateTimes = postDateTimes,
        modifiedDateTime = modifiedDateTime,
    )
}
