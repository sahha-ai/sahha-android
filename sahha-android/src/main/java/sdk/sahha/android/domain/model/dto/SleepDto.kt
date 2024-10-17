package sdk.sahha.android.domain.model.dto

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
internal data class SleepDto(
    val durationInMinutes: Int,
    val startDateTime: String,
    val endDateTime: String,
    val source: String = Constants.SLEEP_DATA_SOURCE,
    val sleepStage: String = Constants.SLEEP_STAGE_IN_BED,
    val createdAt: String = "",
    override val metadata: SahhaMetadata? = null,
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
): HasMetadata<SleepDto> {
    override fun copyWithMetadata(metadata: SahhaMetadata): SleepDto {
        return this.copy(metadata = metadata)
    }
}

internal fun SleepDto.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = id,
        logType = Constants.DataLogs.SLEEP,
        dataType = sleepStage,
        source = source,
        value = durationInMinutes.toDouble(),
        unit = Constants.DataUnits.MINUTE,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        recordingMethod = RecordingMethodsHealthConnect.AUTOMATICALLY_RECORDED.name,
        deviceType = Sahha.di.healthConnectConstantsMapper.devices(Device.TYPE_PHONE),
        metadata = metadata
    )
}
