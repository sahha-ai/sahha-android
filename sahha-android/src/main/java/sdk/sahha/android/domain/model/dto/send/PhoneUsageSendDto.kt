package sdk.sahha.android.domain.model.dto.send

import androidx.annotation.Keep
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethods

@Keep
internal data class PhoneUsageSendDto(
    val isLocked: Boolean,
    val isScreenOn: Boolean,
    val eventTimeStamp: String,
    val source: String = Constants.PHONE_USAGE_DATA_SOURCE,
    val recordingMethod: String = RecordingMethods.AUTOMATICALLY_RECORDED.name,
    val deviceType: String = Constants.DEVICE_TYPE_PHONE
)
