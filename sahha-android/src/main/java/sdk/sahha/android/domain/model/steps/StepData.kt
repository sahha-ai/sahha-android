package sdk.sahha.android.domain.model.steps

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.common.Constants
import java.util.UUID

@Entity
internal data class StepData(
    val source: String,
    val count: Int,
    val detectedAt: String,
    @PrimaryKey val id: String = UUID.nameUUIDFromBytes(
        (getDataType(source) + detectedAt).toByteArray()
    ).toString()
)

internal fun getDataType(source: String): String {
    return when (source) {
        Constants.STEP_COUNTER_DATA_SOURCE -> {
            "total_steps"
        }

        Constants.STEP_DETECTOR_DATA_SOURCE -> {
            "single_step"
        }

        else -> "unknown_steps"
    }
}
