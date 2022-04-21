package sdk.sahha.android.source

import androidx.annotation.Keep

@Keep
class SahhaSettings(
    val environment: Enum<SahhaEnvironment>,
    val framework: SahhaFramework = SahhaFramework.android_kotlin,
    val sensors: Set<Enum<SahhaSensor>> = mutableSetOf<Enum<SahhaSensor>>().let {
        for (sensor in SahhaSensor.values()) {
            it.add(sensor)
        }
        return@let it
    },
    val postSensorDataManually: Boolean = false
)