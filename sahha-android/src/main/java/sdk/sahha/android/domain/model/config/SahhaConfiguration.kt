package sdk.sahha.android.domain.model.config

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaFramework
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSettings

@Entity
internal data class SahhaConfiguration(
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

internal fun SahhaConfiguration.toSahhaSettings(): SahhaSettings {
    return SahhaSettings(
        environment = SahhaEnvironment.values()[environment],
        framework = SahhaFramework.valueOf(framework),
    )
}

internal fun SahhaSettings.toSahhaConfiguration(): SahhaConfiguration {
    return SahhaConfiguration(
        environment = environment.ordinal,
        framework = framework.name,
        sensorArray = arrayListOf(),
        manuallyPostData = false
    )
}

internal fun ArrayList<Int>.toSahhaSensorSet(): Set<SahhaSensor> {
    return mapTo(mutableSetOf()) {
        SahhaSensor.values()[it]
    }
}

internal fun SahhaConfiguration.toSetOfSensors(): Set<SahhaSensor> {
    return sensorArray.mapTo(mutableSetOf()) {
        SahhaSensor.values()[it]
    }
}