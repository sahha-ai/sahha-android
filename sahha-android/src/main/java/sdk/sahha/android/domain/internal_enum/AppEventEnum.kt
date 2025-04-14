package sdk.sahha.android.domain.internal_enum

internal enum class AppEventEnum(val value: String) {
    APP_CREATE("app_create"),
    APP_START("app_start"),
    APP_RESUME("app_resume"),
    APP_PAUSE("app_pause"),
    APP_STOP("app_stop"),
    APP_DESTROY("app_destroy"),
    APP_ALIVE("app_alive"),
}