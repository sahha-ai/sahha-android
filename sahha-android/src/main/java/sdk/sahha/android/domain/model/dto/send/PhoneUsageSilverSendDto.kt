package sdk.sahha.android.domain.model.dto.send

import androidx.annotation.Keep

@Keep
data class PhoneUsageSilverSendDto(
    val lockedCount: Int,
    val unlockedCount: Int,
    val screenOffCount: Int,
    val screenOnCount: Int,
    val startDateTime: String,
    val endDateTime: String
)
