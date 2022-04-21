package sdk.sahha.android.domain.repository

import sdk.sahha.android.source.SahhaActivityStatus

interface PermissionsRepo {
    fun setPermissionLogic()
    fun promptUserToActivateActivityRecognition(callback: ((sahhaActivityStatus: Enum<SahhaActivityStatus>) -> Unit))
    fun activate(callback: ((Enum<SahhaActivityStatus>) -> Unit))
}