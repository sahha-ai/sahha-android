package sdk.sahha.android.domain.repository

import sdk.sahha.android.domain.model.enums.ActivityStatus

interface PermissionsRepo {
    fun setPermissionLogic()
    fun promptUserToActivateActivityRecognition(callback: ((activityStatus: Enum<ActivityStatus>) -> Unit))
    fun activate(callback: ((Enum<ActivityStatus>) -> Unit))
}