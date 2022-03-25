package sdk.sahha.android.domain.repository

import sdk.sahha.android.domain.model.enums.SahhaActivityStatus

interface PermissionsRepo {
    fun setPermissionLogic()
    fun promptUserToActivateActivityRecognition(callback: ((sahhaActivityStatus: Enum<SahhaActivityStatus>) -> Unit))
    fun activate(callback: ((Enum<SahhaActivityStatus>) -> Unit))
}