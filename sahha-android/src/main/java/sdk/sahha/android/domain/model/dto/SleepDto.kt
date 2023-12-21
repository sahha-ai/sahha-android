package sdk.sahha.android.domain.model.dto

import androidx.health.connect.client.records.metadata.Device
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethodsHealthConnect
import sdk.sahha.android.source.Sahha
import java.util.UUID

@Entity
data class SleepDto(
    val durationInMinutes: Int,
    val startDateTime: String,
    val endDateTime: String,
    val source: String = Constants.SLEEP_DATA_SOURCE,
    val sleepStage: String = Constants.SLEEP_STAGE_SLEEPING,
    val createdAt: String = "",
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
)

fun SleepDto.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
        id = id,
        logType = Constants.DataLogs.SLEEP,
        dataType = sleepStage,
        source = source,
        value = durationInMinutes.toDouble(),
        unit = Constants.DataUnits.MINUTE,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        recordingMethod = RecordingMethodsHealthConnect.RECORDING_METHOD_AUTOMATICALLY_RECORDED.name,
        deviceType = Sahha.di.healthConnectConstantsMapper.devices(Device.TYPE_PHONE),
    )
}
