package sdk.sahha.android.domain.model.config

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LastHealthConnectPost(
    @PrimaryKey val id: Int = 1,
    val epochMillis: Long
)
