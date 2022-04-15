package sdk.sahha.android.data

internal object Constants {
    // Configs
    const val TRANSFORMATION = "AES/GCM/NoPadding"
    const val ANDROID_KEY_STORE = "AndroidKeyStore"
    const val UET = "uet"
    const val UERT = "uert"
    const val AUTHORIZATION_HEADER = "Authorization"
    const val PLATFORM_NAME = "android"

    // App Center parameters
    const val SDK_ID = "sdk_id"
    const val SDK_VERSION = "sdk_version"
    const val APP_ID = "app_id"
    const val APP_VERSION = "app_version"
    const val DEVICE_ID = "device_id"
    const val DEVICE_TYPE = "device_type"
    const val DEVICE_MODEL = "device_model"
    const val SYSTEM = "system"
    const val SYSTEM_VERSION = "system_version"
    const val API_AUTH = "api_auth"
    const val API_METHOD = "api_method"
    const val API_URL = "api_url"
    const val API_BODY = "api_body"
    const val ERROR_TYPE = "error_type"
    const val API_ERROR = "api_error"
    const val APPLICATION_ERROR = "application_error"

    // Worker tags
    const val SLEEP_WORKER_TAG = "sleepData"
    const val SLEEP_POST_WORKER_TAG = "sleepPost"
    const val DEVICE_POST_WORKER_TAG = "devicePost"

    // Known values
    const val AVG_STEP_DISTANCE_MALE = 0.78
    const val AVG_STEP_DISTANCE_FEMALE = 0.7
    const val AVG_STEP_DISTANCE = 0.74
    const val ACTIVITY_RECOGNITION_UPDATE_INTERVAL_MILLIS = 15000L

    // Notifications
    const val NOTIFICATION_DATA_COLLECTION = 1000
    const val NOTIFICATION_PERMISSION_SETTINGS = 1001

    // Receivers
    const val ACTIVITY_RECOGNITION_RECEIVER = 2000
    const val SLEEP_DATA_REQUEST = 2002
}
