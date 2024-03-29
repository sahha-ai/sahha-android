package sdk.sahha.android.domain.model.dto

import androidx.annotation.Keep
import androidx.health.connect.client.records.metadata.DeviceTypes
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethodsHealthConnect

@Keep
internal data class StepDto(
    val dataType: String,
    val value: Int,
    val unit: String = Constants.DataUnits.COUNT,
    val source: String,
    val startDateTime: String,
    val endDateTime: String,
    val recordingMethod: String = RecordingMethodsHealthConnect.RECORDING_METHOD_UNKNOWN.name,
    val deviceType: String = Constants.UNKNOWN,
    val modifiedDateTime: String,
    val deviceManufacturer: String? = null,
    val deviceModel: String? = null,
)
