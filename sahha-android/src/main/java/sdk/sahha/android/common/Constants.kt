package sdk.sahha.android.common

internal object Constants {
    const val UNKNOWN = "unknown"

    // Source
    const val SOURCE_MIXED = "mixed"

    // Custom data types
    const val APP_SESSION = "app_session"

    // Prefs
    const val HEALTH_CONNECT_SERVICE_LAUNCHED_KEY = "health.connect.service.launched"
    const val FIRST_HC_REQUEST_KEY = "first.hc.request.key"
    const val CONFIGURATION_PREFS = "configuration_prefs"
    const val ENVIRONMENT_KEY = "environment.key"

    // Action
    const val ACTION_RESTART_SERVICE = "custom.intent.action.RESTART_SERVICE"
    const val ACTION_KILL_SERVICE = "custom.intent.action.KILL_SERVICE"

    // Query
    const val CUSTOM_STEPS_QUERY_ID = "custom.healthconnect.steps.query"
    const val APP_ALIVE_QUERY_ID = "sahha.app.alive.query"
    const val AGGREGATE_QUERY_ID_DAY = "sahha.aggregate.query.day"
    const val AGGREGATE_QUERY_ID_HOUR = "sahha.aggregate.query.hour"
    const val PAGE_TOKEN_SUFFIX = ".initial.query.page.token"

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
    const val NOTIFICATION_TITLE_DEFAULT = "Gathering health insights"
    const val NOTIFICATION_DESC_DEFAULT = "Swipe for options to hide this notification."
    const val FIFTEEN_MINUTES = 15L
    const val THIRTY_MINUTES = 30L
    const val WORKER_REPEAT_1_DAY = 1440L
    const val POST_TIMEOUT_LIMIT_MILLIS = 90L * 1000L

    // Alarm
    const val DEFAULT_ALARM_INTERVAL_MINS = 15L

    // Post limits
    private const val DATA_LOG_SIZE_BYTES = 522 // bytes
    const val DATA_LOG_LIMIT_BYTES = 32 * 1024
    const val DEFAULT_POST_LIMIT = (DATA_LOG_LIMIT_BYTES + DATA_LOG_SIZE_BYTES) / DATA_LOG_SIZE_BYTES
    const val SLEEP_POST_LIMIT = 46
    const val STEP_POST_LIMIT = 45
    const val STEP_SESSION_POST_LIMIT = 40
    const val DEVICE_LOCK_POST_LIMIT = 91
    const val STEP_SESSION_COOLDOWN_MILLIS = 30000L
    const val OKHTTP_CLIENT_TIMEOUT = 30L

    // Tasks
    const val INTENT_ACTION = "sahha.intent.task"
    object IntentAction {
        const val QUERY_HEALTH_CONNECT = "queryHealthConnect"
        const val RESTART_BACKGROUND_TASKS = "resetBackgroundTasks"
    }

    // Sahha Metadata
    const val POST_DATE_TIME = "postDateTime"

    // Data Source
    const val SLEEP_DATA_SOURCE = "AndroidSleep"
    const val STEP_COUNTER_DATA_SOURCE = "AndroidStepCounter"
    const val STEP_DETECTOR_DATA_SOURCE = "AndroidStepDetector"
    const val PHONE_USAGE_DATA_SOURCE = "AndroidScreenStates"

    // Data Units
    object DataUnits {
        const val EMPTY_STRING = ""
        const val BOOLEAN = "boolean"
        const val COUNT = "count"
        const val MMOL_PER_LITRE = "mmol/L"
        const val MG_PER_DL = "mg/dL"
        const val MMHG = "mmHg"
        const val MILLISECOND = "ms"
        const val KILOCALORIE = "kcal"
        const val CELSIUS = "degC"
        const val PERCENTAGE = "percent"
        const val ML_PER_KG_PER_MIN = "mL/kg/min"
        const val KCAL_PER_DAY = "kcal/day"
        const val KILOGRAM = "kg"
        const val INCH = "inch"
        const val METRE = "m"
        const val BREATH_PER_MIN = "breath/min"
        const val BEAT_PER_MIN = "bpm"
        const val STEP_PER_MIN = "step/min" // May be used in future - could be changed to count/min
        const val MINUTE = "minute"
        const val SECOND = "second"
    }

    object DataLogs {
        const val DEMOGRAPHIC = "demographic"
        const val DEVICE = "device"
        const val ACTIVITY = "activity"
        const val SLEEP = "sleep"
        const val BLOOD = "blood"
        const val HEART = "heart"
        const val ENERGY = "energy"
        const val OXYGEN = "oxygen"
        const val BODY = "body"
        const val TEMPERATURE = "temperature"
        const val EXERCISE = "exercise"
    }

    // Sahha Error API parameters
    const val API_ERROR = "api"
    const val APPLICATION_ERROR = "app"

    // Worker tags
    const val SLEEP_WORKER_TAG = "sleepData"
    const val SLEEP_POST_WORKER_TAG = "sleepPost"
    const val DEVICE_POST_WORKER_TAG = "devicePost"
    const val STEP_POST_WORKER_TAG = "stepPost"
    const val SAHHA_DATA_LOG_WORKER_TAG = "sahhaDataLogPost"
    const val HEALTH_CONNECT_POST_WORKER_TAG = "healthConnectPost"
    const val HEALTH_CONNECT_QUERY_WORKER_TAG = "healthConnectQuery"
    const val BACKGROUND_TASK_RESTARTER_WORKER_TAG = "backgroundRestarter"

    // Sleep stage
    const val SLEEP_STAGE_UNKNOWN = "sleep_stage_unknown"
    const val SLEEP_STAGE_AWAKE_IN_OR_OUT_OF_BED = "sleep_stage_awake_in_or_out_of_bed"
    const val SLEEP_STAGE_SLEEPING = "sleep_stage_sleeping"
    const val SLEEP_STAGE_IN_BED = "sleep_stage_in_bed"

    // Device types
    const val DEVICE_TYPE_WATCH = "watch"
    const val DEVICE_TYPE_PHONE = "phone"
    const val DEVICE_TYPE_SCALE = "scale"
    const val DEVICE_TYPE_RING = "ring"
    const val DEVICE_TYPE_HEAD_MOUNTED = "head_mounted"
    const val DEVICE_TYPE_FITNESS_BAND = "fitness_band"
    const val DEVICE_TYPE_CHEST_STRAP = "chest_strap"
    const val DEVICE_TYPE_SMART_DISPLAY = "smart_display"

    // Insights
    const val INSIGHT_NAME_TIME_ASLEEP = "time_asleep_daily_total"
    const val INSIGHT_NAME_TIME_IN_BED = "time_in_bed_daily_total"
    const val INSIGHT_NAME_TIME_IN_REM_SLEEP = "time_in_rem_sleep_daily_total"
    const val INSIGHT_NAME_TIME_IN_LIGHT_SLEEP = "time_in_light_sleep_daily_total"
    const val INSIGHT_NAME_TIME_IN_DEEP_SLEEP = "time_in_deep_sleep_daily_total"
    const val INSIGHT_NAME_STEP_COUNT = "step_count_daily_total"
    const val INSIGHT_NAME_ACTIVE_ENERGY = "active_energy_burned_daily_total"
    const val INSIGHT_NAME_TOTAL_ENERGY = "total_energy_burned_daily_total"

    // Known values
    const val AVG_STEP_DISTANCE_MALE = 0.78
    const val AVG_STEP_DISTANCE_FEMALE = 0.7
    const val AVG_STEP_DISTANCE = 0.74
    const val ACTIVITY_RECOGNITION_UPDATE_INTERVAL_MILLIS = 15000L
    const val ONE_DAY_IN_MILLIS = 86400000L
    const val SEVEN_DAYS_IN_MILLIS = 604800000L

    // Alarm config
    const val ALARM_6PM = 18
    const val ALARM_12AM = 0

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
    const val RESTARTER_RECEIVER = 3002
    const val NOTIFICATION_REQUEST_CODE = 4000
}
