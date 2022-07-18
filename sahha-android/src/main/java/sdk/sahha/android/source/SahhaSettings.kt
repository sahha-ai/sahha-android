package sdk.sahha.android.source

import androidx.annotation.Keep
import sdk.sahha.android.domain.model.config.SahhaNotificationConfiguration

@Keep
class SahhaSettings(
    val environment: Enum<SahhaEnvironment>,
    val notificationSettings: SahhaNotificationConfiguration? = null,
    val framework: SahhaFramework = SahhaFramework.android_kotlin,
    val sensors: Set<Enum<SahhaSensor>>? = null,
    val postSensorDataManually: Boolean = false
)