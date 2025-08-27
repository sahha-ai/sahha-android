package core.config

data class SahhaNotificationConfiguration(
    val icon: Int,              // Resource ID for the notification icon (e.g., R.drawable.ic_notification)
    val title: String,          // Title text for the notification
    val shortDescription: String // Short description or body text for the notification
)