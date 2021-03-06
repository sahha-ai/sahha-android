package sdk.sahha.android.data.remote.dto

import androidx.annotation.Keep

@Keep
data class StepDto(
    val dataType: String,
    val count: Int,
    val source: String,
    val manuallyEntered: Boolean,
    val startDateTime: String,
    val endDateTime: String,
    val createdAt: String
)
