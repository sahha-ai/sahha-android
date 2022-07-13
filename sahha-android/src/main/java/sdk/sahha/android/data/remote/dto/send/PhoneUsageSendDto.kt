package sdk.sahha.android.data.remote.dto.send

import androidx.annotation.Keep

@Keep
data class PhoneUsageSendDto(
    val isLocked: Boolean,
    val isScreenOn: Boolean,
    val createdAt: String
)
