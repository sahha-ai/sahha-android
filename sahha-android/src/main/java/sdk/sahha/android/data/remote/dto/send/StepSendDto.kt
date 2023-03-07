package sdk.sahha.android.data.remote.dto.send

import androidx.annotation.Keep

@Keep
data class StepSendDto(
    val dataType: String,
    val count: Int,
    val source: String,
    val manuallyEntered: Boolean,
    val startDateTime: String,
    val endDateTime: String,
    val createdAt: String
)
