package sdk.sahha.android.domain.model.callbacks

import sdk.sahha.android.source.SahhaSensorStatus

class ActivityCallback {
    var sahhaSensorStatus: Enum<SahhaSensorStatus>? = null
    var requestPermission: ((error: String?, sahhaSensorStatus: Enum<SahhaSensorStatus>) -> Unit)? = null
    var setSettingOnResume: ((sahhaSensorStatus: Enum<SahhaSensorStatus>) -> Unit)? = null
}