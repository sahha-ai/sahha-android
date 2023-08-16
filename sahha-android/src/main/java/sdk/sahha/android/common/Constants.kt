package sdk.sahha.android.data

internal object Constants {
    // Action
    const val ACTION_RESTART_SERVICE = "custom.intent.action.RESTART_SERVICE"

    // Configs
    const val TRANSFORMATION = "AES/GCM/NoPadding"
    const val ANDROID_KEY_STORE = "AndroidKeyStore"
    const val ALIAS_UUID = "alias_uuid"
    const val UET = "uet"
    const val UERT = "uert"
    const val AUTHORIZATION_HEADER = "Authorization"
    const val APP_ID_HEADER = "AppId"
    const val APP_SECRET_HEADER = "AppSecret"
    const val PLATFORM_NAME = "android"
    const val MAX_STEP_POST_VALUE = 1000
    const val SENSOR_SHARED_PREF_KEY = "device.sensor.status"
    const val NOTIFICATION_TITLE_DEFAULT = "Analytics are running"
    const val NOTIFICATION_DESC_DEFAULT = "Swipe for options to hide this notification."
    const val DEFAULT_WORKER_REPEAT_INTERVAL_MINUTES = 15L
    const val SILVER_DATA_WORKER_REPEAT_INTERVAL_MINUTES = 60L
    const val SLEEP_POST_LIMIT = 46
    const val STEP_POST_LIMIT = 45
    const val STEP_SESSION_POST_LIMIT = 40
    const val DEVICE_LOCK_POST_LIMIT = 91
    const val STEP_SESSION_COOLDOWN_MILLIS = 30000L

    // Data Source
    const val SLEEP_DATA_SOURCE = "AndroidSleep"
    const val STEP_COUNTER_DATA_SOURCE = "AndroidStepCounter"
    const val STEP_DETECTOR_DATA_SOURCE = "AndroidStepDetector"

    // Data Type
    const val STEP_COUNTER_DATA_TYPE = "TotalSteps"
    const val STEP_DETECTOR_DATA_TYPE = "SingleStep"
    const val CUSTOM_STEP_SESSION_DATA_TYPE = "CustomStepSessions"
    const val HOURLY_SINGLE_STEP_DATA_TYPE = "SingleStepHourly"

    // Sahha Error API parameters
    const val API_ERROR = "api"
    const val APPLICATION_ERROR = "app"

    // Worker tags
    const val SLEEP_WORKER_TAG = "sleepData"
    const val SLEEP_POST_WORKER_TAG = "sleepPost"
    const val DEVICE_POST_WORKER_TAG = "devicePost"
    const val STEP_POST_WORKER_TAG = "stepPost"
    const val HOURLY_STEP_POST_WORKER_TAG = "stepPostHourly"
    const val HOURLY_DEVICE_POST_WORKER_TAG = "devicePostHourly"

    // Known values
    const val AVG_STEP_DISTANCE_MALE = 0.78
    const val AVG_STEP_DISTANCE_FEMALE = 0.7
    const val AVG_STEP_DISTANCE = 0.74
    const val ACTIVITY_RECOGNITION_UPDATE_INTERVAL_MILLIS = 15000L
    const val ONE_DAY_IN_MILLIS = 86400000L
    const val SEVEN_DAYS_IN_MILLIS = 604800000L

    // Notifications
    const val NOTIFICATION_DATA_COLLECTION = 1000
    const val NOTIFICATION_PERMISSION_SETTINGS = 1001

    // Receivers
    const val ACTIVITY_RECOGNITION_RECEIVER = 2000
    const val SLEEP_DATA_REQUEST = 2002
}
