package sdk.sahha.android.domain.model.config

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SahhaConfiguration(
    @PrimaryKey val id: Int,
    val clientId: String,
    val clientSecret: String,
    val environment: Int,
    val sensorArray: ArrayList<Int>,
    val manuallyPostData: Boolean
) {
    constructor(
        clientId: String,
        clientSecret: String,
        environment: Int,
        sensorArray: ArrayList<Int>,
        manuallyPostData: Boolean
    ) : this(
        1,
        clientId,
        clientSecret,
        environment,
        sensorArray,
        manuallyPostData
    )
}
