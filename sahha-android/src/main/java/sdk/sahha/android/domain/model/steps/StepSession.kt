package sdk.sahha.android.domain.model.steps

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.domain.model.metadata.HasMetadata
import java.util.UUID

@Entity
internal data class StepSession(
    val count: Int,
    val startDateTime: String,
    val endDateTime: String,
    override val postDateTimes: ArrayList<String>? = null,
    override val modifiedDateTime: String? = null,
    @PrimaryKey val id: String = UUID.nameUUIDFromBytes(
        "StepSession$startDateTime$endDateTime"
            .toByteArray()
    ).toString(),
) : HasMetadata<StepSession> {
    override fun copyWithMetadata(
        postDateTimes: ArrayList<String>?,
        modifiedDateTime: String?,
    ): StepSession {
        return this.copy(
            postDateTimes = postDateTimes,
            modifiedDateTime = modifiedDateTime,
        )
    }
}
