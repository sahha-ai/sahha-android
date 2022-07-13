package sdk.sahha.android.domain.model.device

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.data.remote.dto.send.PhoneUsageSendDto


@Entity
data class PhoneUsage(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val isLocked: Boolean,
    val isScreenOn: Boolean,
    val createdAt: String
) {
    constructor(isLocked: Boolean, isScreenOn: Boolean, createdAt: String)
            : this(0, isLocked, isScreenOn, createdAt)
}

fun PhoneUsage.toPhoneUsageSendDto(): PhoneUsageSendDto {
    return PhoneUsageSendDto(
        isLocked,
        isScreenOn,
        createdAt
    )
}
