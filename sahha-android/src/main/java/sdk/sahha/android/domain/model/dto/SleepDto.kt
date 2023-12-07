package sdk.sahha.android.domain.model.dto

import android.os.Build
import androidx.health.connect.client.records.metadata.Device
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethodsHealthConnect
import sdk.sahha.android.domain.model.dto.send.SleepSendDto
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

fun SleepDto.toSleepSendDto(): SleepSendDto {
    return SleepSendDto(
        source = source,
        durationInMinutes = durationInMinutes,
        sleepStage = sleepStage,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        recordingMethod = RecordingMethodsHealthConnect.RECORDING_METHOD_AUTOMATICALLY_RECORDED.name,
        deviceType = Sahha.di.healthConnectConstantsMapper.devices(Device.TYPE_PHONE),
        deviceManufacturer = Build.MANUFACTURER,
        deviceModel = Build.MODEL
    )
}
