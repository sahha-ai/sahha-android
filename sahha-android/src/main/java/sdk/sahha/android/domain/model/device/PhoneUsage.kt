package sdk.sahha.android.domain.model.device

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.domain.model.metadata.HasMetadata
import java.util.UUID


@Entity
internal data class PhoneUsage(
    val isLocked: Boolean,
    val isScreenOn: Boolean,
    val createdAt: String,
    override val postDateTimes: ArrayList<String>? = null,
    override val modifiedDateTime: String? = null,
    @PrimaryKey val id: String = UUID.nameUUIDFromBytes(
        ("PhoneUsage$createdAt").toByteArray()
    ).toString()
) : HasMetadata<PhoneUsage> {
    override fun copyWithMetadata(
        postDateTimes: ArrayList<String>?,
        modifiedDateTime: String?,
    ): PhoneUsage {
        return this.copy(
            postDateTimes = postDateTimes,
            modifiedDateTime = modifiedDateTime,
        )
    }
}