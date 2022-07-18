package sdk.sahha.android.domain.model.config

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.R

@Keep
@Entity
data class SahhaNotificationConfiguration(
    @PrimaryKey val id: Int,
    val icon: Int,
    val title: String,
    val shortDescription: String
) {
    constructor(
        icon: Int = R.drawable.ic_sahha_no_bg,
        title: String = "Analytics are running",
        shortDescription: String = "Swipe for options to hide this notification."
    ) : this(
        1,
        icon,
        title,
        shortDescription
    )
}