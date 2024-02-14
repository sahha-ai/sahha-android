package sdk.sahha.android.domain.model.permissions

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class ManualPermission(
    @PrimaryKey val sensorEnum: Int,
    val statusEnum: Int
)
