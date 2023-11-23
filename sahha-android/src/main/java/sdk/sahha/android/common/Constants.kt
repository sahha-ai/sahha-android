package sdk.sahha.android.common

internal object Constants {
    const val UNKNOWN = "UNKNOWN"
    const val AWAKE_IN_OR_OUT_OF_BED = "awake_in_or_out_of_bed"

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
    const val NOTIFICATION_TITLE_DEFAULT = "Gathering health insights"
    const val NOTIFICATION_DESC_DEFAULT = "Swipe for options to hide this notification."
    const val WORKER_REPEAT_INTERVAL_MINUTES = 15L
    const val POST_TIMEOUT_LIMIT_MILLIS = 300000L

    // Alarm
    const val DEFAULT_ALARM_INTERVAL_MINS = 15L
    const val DEFAULT_INITIAL_ALARM_DELAY_SECS = 10L

    // Post limits
    const val DEFAULT_POST_LIMIT = 25
    const val SLEEP_POST_LIMIT = 46
    const val STEP_POST_LIMIT = 45
    const val STEP_SESSION_POST_LIMIT = 40
    const val DEVICE_LOCK_POST_LIMIT = 91
    const val STEP_SESSION_COOLDOWN_MILLIS = 30000L
    const val OKHTTP_CLIENT_TIMEOUT = 30L

    // Data Source
    const val SLEEP_DATA_SOURCE = "AndroidSleep"
    const val STEP_COUNTER_DATA_SOURCE = "AndroidStepCounter"
    const val STEP_DETECTOR_DATA_SOURCE = "AndroidStepDetector"
    const val PHONE_USAGE_DATA_SOURCE = "AndroidScreenStates"

    // Data Type
    const val STEP_COUNTER_DATA_TYPE = "TotalSteps"
    const val STEP_DETECTOR_DATA_TYPE = "SingleStep"
    const val CUSTOM_STEP_SESSION_DATA_TYPE = "CustomStepSessions"
    const val HEALTH_CONNECT_STEP_DATA_TYPE = "HealthConnectSteps"
    const val HEALTH_CONNECT_BLOOD_GLUCOSE = "HealthConnectBloodGlucose"
    const val HEALTH_CONNECT_BLOOD_PRESSURE_SYSTOLIC = "HealthConnectBloodPressureSystolic"
    const val HEALTH_CONNECT_BLOOD_PRESSURE_DIASTOLIC = "HealthConnectBloodPressureDiastolic"
    const val HEALTH_CONNECT_HEART_RATE = "HealthConnectHeartRate"
    const val HEALTH_CONNECT_HEART_RATE_AVG = "HealthConnectHeartRateAvg"
    const val HEALTH_CONNECT_HEART_RATE_MIN = "HealthConnectHeartRateMin"
    const val HEALTH_CONNECT_HEART_RATE_MAX = "HealthConnectHeartRateMax"
    const val HEALTH_CONNECT_RESTING_HEART_RATE = "HealthConnectRestingHeartRate"
    const val HEALTH_CONNECT_RESTING_HEART_RATE_AVG = "HealthConnectRestingHeartRateAvg"
    const val HEALTH_CONNECT_RESTING_HEART_RATE_MIN = "HealthConnectRestingHeartRateMin"
    const val HEALTH_CONNECT_RESTING_HEART_RATE_MAX = "HealthConnectRestingHeartRateMax"
    const val HEALTH_CONNECT_HEART_RATE_VARIABILITY_RMSSD = "HealthConnectHeartRateVariabilityRmssd"

    // Data Units
    const val HEALTH_CONNECT_UNIT_MMOL_PER_LITRE = "mmol/L"
    const val HEALTH_CONNECT_UNIT_MMHG = "mmHg"
    const val HEALTH_CONNECT_UNIT_MILLISECONDS = "milliseconds"

    // Sahha Error API parameters
    const val API_ERROR = "api"
    const val APPLICATION_ERROR = "app"

    // Worker tags
    const val SLEEP_WORKER_TAG = "sleepData"
    const val SLEEP_POST_WORKER_TAG = "sleepPost"
    const val DEVICE_POST_WORKER_TAG = "devicePost"
    const val STEP_POST_WORKER_TAG = "stepPost"
    const val HEALTH_CONNECT_POST_WORKER_TAG = "healthConnectPost"

    // Sleep stage
    const val SLEEP_STAGE_SLEEPING = "sleeping"

    // Device types
    const val DEVICE_TYPE_WATCH = "WATCH"
    const val DEVICE_TYPE_PHONE = "PHONE"
    const val DEVICE_TYPE_SCALE = "SCALE"
    const val DEVICE_TYPE_RING = "RING"
    const val DEVICE_TYPE_HEAD_MOUNTED = "HEAD_MOUNTED"
    const val DEVICE_TYPE_FITNESS_BAND = "FITNESS_BAND"
    const val DEVICE_TYPE_CHEST_STRAP = "CHEST_STRAP"
    const val DEVICE_TYPE_SMART_DISPLAY = "SMART_DISPLAY"

    // Insights
    const val INSIGHT_NAME_TIME_ASLEEP = "TimeAsleepDailyTotal"
    const val INSIGHT_NAME_TIME_IN_BED = "TimeInBedDailyTotal"
    const val INSIGHT_NAME_TIME_IN_REM_SLEEP = "TimeInREMSleepDailyTotal"
    const val INSIGHT_NAME_TIME_IN_LIGHT_SLEEP = "TimeInLightSleepDailyTotal"
    const val INSIGHT_NAME_TIME_IN_DEEP_SLEEP = "TimeInDeepSleepDailyTotal"
    const val INSIGHT_NAME_STEP_COUNT = "StepCountDailyTotal"
    const val UNIT_MINUTES = "minutes"
    const val UNIT_STEPS = "steps"

    // Known values
    const val AVG_STEP_DISTANCE_MALE = 0.78
    const val AVG_STEP_DISTANCE_FEMALE = 0.7
    const val AVG_STEP_DISTANCE = 0.74
    const val ACTIVITY_RECOGNITION_UPDATE_INTERVAL_MILLIS = 15000L
    const val ONE_DAY_IN_MILLIS = 86400000L
    const val SEVEN_DAYS_IN_MILLIS = 604800000L

    // Insights config
    const val INSIGHTS_SLEEP_ALARM_HOUR = 18
    const val INSIGHTS_STEPS_ALARM_HOUR = 0

    // Notifications
    const val NOTIFICATION_DATA_COLLECTION = 1000
    const val NOTIFICATION_HEALTH_CONNECT = 1002
    const val NOTIFICATION_INSIGHTS = 1004
    const val NOTIFICATION_PERMISSION_SETTINGS = 1003
    const val HEALTH_CONNECT_NOTIFICATION_CHANNEL_ID = "sahha.healthconnect.service"
    const val INSIGHTS_NOTIFICATION_CHANNEL_ID = "sahha.insights.service"
    const val TEMP_FOREGROUND_NOTIFICATION_DURATION_MILLIS = 5000L

    // Receivers and Request Codes
    const val ACTIVITY_RECOGNITION_RECEIVER = 2000
    const val SLEEP_DATA_REQUEST = 2002
    const val HEALTH_CONNECT_QUERY_RECEIVER = 3000
    const val INSIGHTS_QUERY_RECEIVER = 3001
    const val NOTIFICATION_REQUEST_CODE = 4000
}
