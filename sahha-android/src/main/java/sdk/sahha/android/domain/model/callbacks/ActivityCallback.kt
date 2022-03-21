package sdk.sahha.android.domain.model.callbacks

import sdk.sahha.android.domain.model.enums.ActivityStatus

class ActivityCallback {
    var activityStatus: Enum<ActivityStatus>? = null
    var requestPermission: ((activityStatus: Enum<ActivityStatus>) -> Unit)? = null
    var setSettingOnResume: ((activityStatus: Enum<ActivityStatus>) -> Unit)? = null
}