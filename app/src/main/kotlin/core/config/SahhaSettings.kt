package core.config

data class SahhaSettings(
    val environment: SahhaEnvironment = SahhaEnvironment.PRODUCTION, // The environment (e.g., SANDBOX or PRODUCTION)
    val notificationSettings: SahhaNotificationConfiguration? = null // Optional notification config; defaults to null for system defaults
)