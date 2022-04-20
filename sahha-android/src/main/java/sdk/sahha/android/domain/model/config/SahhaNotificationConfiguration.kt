package sdk.sahha.android.domain.model.config

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SahhaNotificationConfiguration(
    @PrimaryKey val id: Int,
    val icon: Int,
    val title: String,
    val shortDescription: String
) {
    constructor(
        icon: Int,
        title: String,
        shortDescription: String
    ) : this(
        1,
        icon,
        title,
        shortDescription
    )
}