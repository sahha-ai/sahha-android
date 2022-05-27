package sdk.sahha.android.domain.model.error_log

data class SahhaErrorLog(
    val sdkId: String?,
    val sdkVersion: String?,
    val appId: String?,
    val appVersion: String?,
    val deviceId: String?,
    val deviceType: String?,
    val deviceModel: String?,
    val system: String?,
    val systemVersion: String?,
    val errorCode: Int?,
    val errorType: String?,
    val errorMessage: String?,
    val errorSource: String?,
    val apiURL: String?,
    val apiMethod: String?,
    val apiBody: String?,
    val appMethod: String?,
    val appBody: String?
)
