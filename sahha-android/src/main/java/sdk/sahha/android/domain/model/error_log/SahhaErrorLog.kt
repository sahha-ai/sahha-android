package sdk.sahha.android.domain.model.error_log

import androidx.annotation.Keep

@Keep
data class SahhaErrorLog(
    var sdkId: String? = null,
    var sdkVersion: String? = null,
    var appId: String? = null,
    var appVersion: String? = null,
    var deviceId: String? = null,
    var deviceType: String? = null,
    var deviceModel: String? = null,
    var system: String? = null,
    var systemVersion: String? = null,
    var errorCode: Int? = null,
    var errorType: String? = null,
    var errorMessage: String? = null,
    var errorSource: String? = null,
    var apiURL: String? = null,
    var apiMethod: String? = null,
    var apiBody: String? = null,
    var appMethod: String? = null,
    var appBody: String? = null
)
