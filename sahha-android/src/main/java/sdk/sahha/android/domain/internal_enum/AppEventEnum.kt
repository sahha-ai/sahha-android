package sdk.sahha.android.domain.internal_enum

internal enum class AppEventEnum(val event: String) {
    APP_LAUNCH("app_launch"),
    APP_OPEN("app_open"),
    APP_CLOSE("app_close"),
    APP_TERMINATE("app_terminate"),
    APP_ALIVE("app_alive"),
}