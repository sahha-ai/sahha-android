package sdk.sahha.android.source

import androidx.annotation.Keep

@Keep
class SahhaSettings(
    val environment: Enum<SahhaEnvironment>,
    val notificationSettings: SahhaNotificationConfiguration? = null,
    val framework: SahhaFramework = SahhaFramework.android_kotlin,
)