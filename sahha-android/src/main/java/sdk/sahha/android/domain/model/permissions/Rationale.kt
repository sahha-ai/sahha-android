package sdk.sahha.android.domain.model.permissions

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class Rationale(
    @PrimaryKey val sensorType: Int,
    val denialCount: Int,
)
