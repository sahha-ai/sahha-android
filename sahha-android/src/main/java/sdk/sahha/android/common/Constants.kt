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
    object DataTypes {
        const val STEP_COUNTER = "TotalSteps"
        const val STEP_DETECTOR = "SingleStep"
        const val SAHHA_STEP_SESSION = "CustomStepSessions"
        const val STEP = "StepCount"
        const val BLOOD_GLUCOSE = "BloodGlucose"
        const val BLOOD_PRESSURE_SYSTOLIC = "BloodPressureSystolic"
        const val BLOOD_PRESSURE_DIASTOLIC = "BloodPressureDiastolic"
        const val HEART_RATE = "HeartRate"
        const val HEART_RATE_AVG = "HeartRateAvg" // Not currently used
        const val HEART_RATE_MIN = "HeartRateMin" // Not currently used
        const val HEART_RATE_MAX = "HeartRateMax" // Not currently used
        const val RESTING_HEART_RATE = "RestingHeartRate"
        const val RESTING_HEART_RATE_AVG = "RestingHeartRateAvg" // Not currently used
        const val RESTING_HEART_RATE_MIN = "RestingHeartRateMin" // Not currently used
        const val RESTING_HEART_RATE_MAX = "RestingHeartRateMax" // Not currently used
        const val HEART_RATE_VARIABILITY = "HeartRateVariability"
        const val ACTIVE_CALORIES_BURNED = "ActiveCaloriesBurned"
        const val OXYGEN_SATURATION = "OxygenSaturation"
        const val TOTAL_CALORIES_BURNED = "TotalCaloriesBurned"
        const val VO2_MAX = "Vo2Max"
        const val BASAL_METABOLIC_RATE = "BasalMetabolicRate"
        const val BODY_FAT = "BodyFat"
        const val BODY_WATER_MASS = "BodyWaterMass"
        const val LEAN_BODY_MASS = "LeanBodyMass"
        const val HEIGHT = "Height"
        const val WEIGHT = "Weight"
        const val RESPIRATORY_RATE = "RespiratoryRate"
        const val BONE_MASS = "BoneMass"
    }

    // Data Units
    object DataUnits {
        const val MMOL_PER_LITRE = "mmol/L"
        const val MMHG = "mmHg"
        const val MILLISECONDS = "milliseconds"
        const val CALORIES = "calories"
        const val CELSIUS = "celsius"
        const val FLOORS = "floors"
        const val PERCENTAGE = "percentage"
        const val ML_PER_KG_PER_MIN = "mL/kg/min"
        const val KCAL_PER_DAY = "kcal/day"
        const val GRAMS = "grams"
        const val KILOGRAMS = "kilograms"
        const val INCHES = "inches"
        const val METRES = "metres"
        const val BREATHS_PER_MIN = "breaths/min"
        const val BEATS_PER_MIN = "bpm"
        const val STEPS_PER_MIN = "steps/min"
        const val MINUTES = "minutes"
        const val STEPS = "steps"
    }

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
    const val INSIGHT_NAME_STEP_COUNT = "StepCountDailyTotal"

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
