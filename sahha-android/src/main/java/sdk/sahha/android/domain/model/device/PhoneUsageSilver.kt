package sdk.sahha.android.domain.model.device

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.domain.model.dto.send.PhoneUsageSendDto

@Entity
data class PhoneUsageSilver(
    val isLocked: Boolean,
    val isScreenOn: Boolean,
    val detectedAt: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)
