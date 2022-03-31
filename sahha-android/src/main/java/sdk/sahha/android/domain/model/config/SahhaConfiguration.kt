package sdk.sahha.android.domain.model.config

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SahhaConfiguration(
    @PrimaryKey val id: Int,
    val environment: Int,
    val sensorArray: ArrayList<Int>,
    val manuallyPostData: Boolean
) {
    constructor(
        environment: Int,
        sensorArray: ArrayList<Int>,
        manuallyPostData: Boolean
    ) : this(
        1,
        environment,
        sensorArray,
        manuallyPostData
    )
}
