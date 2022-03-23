package sdk.sahha.android.data

internal object Constants {
    // Configs
    const val BASE_URL = "https://sahhaapi-sandbox.azurewebsites.net/api/"
    const val TRANSFORMATION = "AES/GCM/NoPadding"
    const val ANDROID_KEY_STORE = "AndroidKeyStore"
    const val UET = "uet"

    // Known values
    const val AVG_STEP_DISTANCE_MALE = 0.78
    const val ACTIVITY_RECOGNITION_UPDATE_INTERVAL_MILLIS = 15000L

    // Notifications
    const val NOTIFICATION_DATA_COLLECTION = 1000
    const val NOTIFICATION_PERMISSION_SETTINGS = 1001

    // Receivers
    const val ACTIVITY_RECOGNITION_RECEIVER = 2000
}
