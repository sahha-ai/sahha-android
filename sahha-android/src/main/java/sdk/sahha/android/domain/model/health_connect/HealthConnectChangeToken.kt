package sdk.sahha.android.domain.model.health_connect

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class HealthConnectChangeToken(
    @PrimaryKey val recordType: String,
    val token: String?
)
