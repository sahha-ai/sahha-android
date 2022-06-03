package sdk.sahha.android.domain.model.device

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class DeviceUsage(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val createdAtEpoch: Long,
    val isLocked: Boolean,
) {
    constructor(createdAtEpoch: Long, isLocked: Boolean) : this(0, createdAtEpoch, isLocked)
}
