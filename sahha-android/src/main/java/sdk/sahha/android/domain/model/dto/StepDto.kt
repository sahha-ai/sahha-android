package sdk.sahha.android.domain.model.dto

import androidx.annotation.Keep
import androidx.health.connect.client.records.metadata.DeviceTypes
import sdk.sahha.android.data.Constants

@Keep
data class StepDto(
    val dataType: String,
    val count: Int,
    val source: String,
    val startDateTime: String,
    val endDateTime: String,
    val recordingMethod: String = Constants.UNKNOWN,
    val sourceDevice: String = DeviceTypes.UNKNOWN,
    val modifiedDateTime: String,
    val deviceManufacturer: String? = null,
    val deviceModel: String? = null,
)
