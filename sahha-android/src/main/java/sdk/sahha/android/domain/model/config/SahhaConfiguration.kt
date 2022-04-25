package sdk.sahha.android.domain.model.config

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaFramework
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSettings

@Entity
data class SahhaConfiguration(
    @PrimaryKey val id: Int,
    val environment: Int,
    val framework: String,
    val sensorArray: ArrayList<Int>,
    val postSensorDataManually: Boolean
) {
    constructor(
        environment: Int,
        framework: String,
        sensorArray: ArrayList<Int>,
        manuallyPostData: Boolean
    ) : this(
        1,
        environment,
        framework,
        sensorArray,
        manuallyPostData
    )
}

fun SahhaConfiguration.toSahhaSettings(): SahhaSettings {
    return SahhaSettings(
        SahhaEnvironment.values()[environment],
        SahhaFramework.valueOf(framework),
        sensorArray.mapTo(mutableSetOf()) {
            SahhaSensor.values()[it]
        },
        postSensorDataManually
    )
}
