package sdk.sahha.android.domain.model.dto

import androidx.health.connect.client.records.metadata.Device
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethodsHealthConnect
import sdk.sahha.android.source.Sahha

@Entity
data class SleepDto(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val source: String,
    val durationInMinutes: Int,
    val sleepStage: String,
    val startDateTime: String,
    val endDateTime: String,
    val createdAt: String = "",
) {
    constructor(
        durationInMinutes: Int,
        startDateTime: String,
        endDateTime: String,
    ) : this(
        0,
        Constants.SLEEP_DATA_SOURCE,
        durationInMinutes,
        Constants.SLEEP_STAGE_SLEEPING,
        startDateTime,
        endDateTime,
        ""
    )
}

fun SleepDto.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
        logType = Constants.DataLogs.SLEEP,
        dataType = Constants.DataTypes.SLEEP,
        source = source,
        value = durationInMinutes.toDouble(),
        unit = Constants.DataUnits.MINUTE,
        additionalProperties = hashMapOf(
            "sleepStage" to sleepStage
        ),
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        recordingMethod = RecordingMethodsHealthConnect.RECORDING_METHOD_AUTOMATICALLY_RECORDED.name,
        deviceType = Sahha.di.healthConnectConstantsMapper.devices(Device.TYPE_PHONE),
    )
}
