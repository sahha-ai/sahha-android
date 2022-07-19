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
        icon: Int? = null,
        title: String? = null,
        shortDescription: String? = null
    ) : this(
        1,
        icon ?: R.drawable.ic_sahha_no_bg,
        title ?: "Analytics are running",
        shortDescription ?: "Swipe for options to hide this notification."
    )
}