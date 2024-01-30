package sdk.sahha.android.domain.model.callbacks

import sdk.sahha.android.source.SahhaSensorStatus

internal class ActivityCallback {
    var sahhaSensorStatus: Enum<SahhaSensorStatus>? = null
    var statusCallback: ((error: String?, sahhaSensorStatus: Enum<SahhaSensorStatus>) -> Unit)? = null
    var setSettingOnResume: ((sahhaSensorStatus: Enum<SahhaSensorStatus>) -> Unit)? = null
}