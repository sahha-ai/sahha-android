package sdk.sahha.android.domain.model.config

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SahhaConfiguration(
    @PrimaryKey val id: Int,
    val environment: Int,
    val sensorArray: ArrayList<Int>,
    val autoPostData: Boolean
) {
    constructor(
        environment: Int,
        sensorArray: ArrayList<Int>,
        autoPostData: Boolean
    ) : this(
        1,
        environment,
        sensorArray,
        autoPostData
    )
}
