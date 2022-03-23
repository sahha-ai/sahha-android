package sdk.sahha.android.domain.model.device

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class PhoneUsage(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @SerializedName("isLocked")
    val isLocked: Boolean,
    @SerializedName("createdAt")
    val createdAt: String
) {
    constructor(isLocked: Boolean, createdAt: String) : this(0, isLocked, createdAt)
}
