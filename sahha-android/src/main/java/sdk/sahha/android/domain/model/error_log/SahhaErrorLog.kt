package sdk.sahha.android.domain.model.error_log

import androidx.annotation.Keep

@Keep
data class SahhaErrorLog(
    var sdkId: String?,
    var sdkVersion: String?,
    var appId: String?,
    var appVersion: String?,
    var deviceId: String?,
    var deviceType: String?,
    var deviceModel: String?,
    var system: String?,
    var systemVersion: String?,
    var errorCode: Int?,
    var errorLocation: String?,
    var errorMessage: String?,
    var errorSource: String?,
    var errorBody: String?,
    var codePath: String?,
    var codeMethod: String?,
    var codeBody: String?
)
