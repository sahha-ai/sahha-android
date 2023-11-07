package sdk.sahha.android.source

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.R
import sdk.sahha.android.common.Constants.NOTIFICATION_DESC_DEFAULT
import sdk.sahha.android.common.Constants.NOTIFICATION_TITLE_DEFAULT

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
        title?.ifEmpty { NOTIFICATION_TITLE_DEFAULT } ?: NOTIFICATION_TITLE_DEFAULT,
        shortDescription?.ifEmpty { NOTIFICATION_DESC_DEFAULT } ?: NOTIFICATION_DESC_DEFAULT
    )
}