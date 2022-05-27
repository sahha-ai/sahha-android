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
    const val SDK_ID = "sdkId"
    const val SDK_VERSION = "sdkVersion"
    const val APP_ID = "appId"
    const val APP_VERSION = "appVersion"
    const val DEVICE_ID = "deviceId"
    const val DEVICE_TYPE = "deviceType"
    const val DEVICE_MODEL = "deviceModel"
    const val SYSTEM = "system"
    const val SYSTEM_VERSION = "systemVersion"
    const val API_METHOD = "apiMethod"
    const val API_URL = "apiURL"
    const val API_BODY = "apiBody"
    const val ERROR_TYPE = "errorType"
    const val ERROR_CODE = "errorCode"
    const val APP_METHOD = "appMethod"
    const val APP_BODY = "appBody"
    const val ERROR_MESSAGE = "errorMessage"
    const val ERROR_SOURCE = "errorSource"
    const val API_ERROR = "api"
    const val APPLICATION_ERROR = "application"

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
