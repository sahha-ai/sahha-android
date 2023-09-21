package sdk.sahha.android.domain.model.health_connect

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HealthConnectQuery(
    @PrimaryKey val id: String,
    val lastSuccessfulTimeStampEpochMillis: Long,
)