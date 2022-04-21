package sdk.sahha.android.domain.model.callbacks

import sdk.sahha.android.source.SahhaActivityStatus

class ActivityCallback {
    var sahhaActivityStatus: Enum<SahhaActivityStatus>? = null
    var requestPermission: ((sahhaActivityStatus: Enum<SahhaActivityStatus>) -> Unit)? = null
    var setSettingOnResume: ((sahhaActivityStatus: Enum<SahhaActivityStatus>) -> Unit)? = null
}