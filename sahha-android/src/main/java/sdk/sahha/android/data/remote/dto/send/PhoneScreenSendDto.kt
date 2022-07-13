package sdk.sahha.android.data.remote.dto.send

import androidx.annotation.Keep

@Keep
data class PhoneScreenSendDto(
    val isLocked: Boolean,
    val isScreenOn: Boolean,
    val createdAt: String
)
