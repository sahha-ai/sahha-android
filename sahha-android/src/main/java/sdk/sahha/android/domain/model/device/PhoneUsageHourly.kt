package sdk.sahha.android.domain.model.device

import sdk.sahha.android.domain.model.dto.send.PhoneUsageSilverSendDto

data class PhoneUsageHourly(
    val lockCount: Int,
    val unlockCount: Int,
    val screenOnCount: Int,
    val screenOffCount: Int,
    val start: String,
    val end: String
)

fun PhoneUsageHourly.toPhoneUsageSilverSendDto(): PhoneUsageSilverSendDto {
    return PhoneUsageSilverSendDto(
        lockCount, unlockCount, screenOnCount, screenOffCount, start, end
    )
}
