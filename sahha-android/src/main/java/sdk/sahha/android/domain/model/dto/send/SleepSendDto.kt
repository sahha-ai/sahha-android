package sdk.sahha.android.domain.model.dto.send

import androidx.annotation.Keep
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethods

@Keep
internal data class SleepSendDto(
    val source: String,
    val durationInMinutes: Int? = null,
    val sleepStage: String? = Constants.SLEEP_STAGE_SLEEPING,
    val startDateTime: String,
    val endDateTime: String,
    val recordingMethod: String = RecordingMethods.unknown.name,
    val deviceType: String = Constants.UNKNOWN,
    val modifiedDateTime: String = endDateTime,
    val deviceManufacturer: String = Constants.UNKNOWN,
    val deviceModel: String = Constants.UNKNOWN,
)