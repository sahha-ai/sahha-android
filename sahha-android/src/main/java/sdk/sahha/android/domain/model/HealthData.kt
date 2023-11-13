package sdk.sahha.android.domain.model

import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethodsHealthConnect

open class HealthData(
    open val dataType: String,
    open val count: Long,
    open val source: String,
    open val startDateTime: String,
    open val endDateTime: String,
    open val recordingMethod: String? = RecordingMethodsHealthConnect.RECORDING_METHOD_UNKNOWN.name,
    open val unit: String? = null,
    open val deviceType: String = Constants.UNKNOWN,
    open val modifiedDateTime: String? = null,
    open val deviceManufacturer: String = Constants.UNKNOWN,
    open val deviceModel: String = Constants.UNKNOWN
)
