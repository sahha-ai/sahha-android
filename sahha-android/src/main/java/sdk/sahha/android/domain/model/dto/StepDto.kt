package sdk.sahha.android.domain.model.dto

import android.health.connect.datatypes.Metadata
import androidx.annotation.Keep

@Keep
data class StepDto(
    val dataType: String,
    val count: Int,
    val source: String,
    val manuallyEntered: Boolean,
    val startDateTime: String,
    val endDateTime: String,
    val recordingMethod: String = "RECORDING_METHOD_UNKNOWN"
)
