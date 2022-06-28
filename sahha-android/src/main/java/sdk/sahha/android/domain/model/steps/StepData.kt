package sdk.sahha.android.domain.model.steps

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class StepData(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val count: Int,
    val detectedAt: String
) {
    constructor(
        count: Int,
        detectedAt: String
    ) : this(
        0,
        count,
        detectedAt
    )
}
