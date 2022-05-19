package sdk.sahha.android.source

import androidx.annotation.Keep

@Keep
class SahhaSettings(
    val environment: Enum<SahhaEnvironment>,
    val framework: SahhaFramework = SahhaFramework.android_kotlin,
    val sensors: Set<Enum<SahhaSensor>>? = null,
    val postSensorDataManually: Boolean = false
)