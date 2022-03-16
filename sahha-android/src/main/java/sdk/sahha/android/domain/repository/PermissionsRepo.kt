package sdk.sahha.android.domain.repository

import sdk.sahha.android.domain.model.PermissionCallback

interface PermissionsRepo {
    fun setPermissionLogic()
    fun setPermissionLogic(permissionCallback: PermissionCallback)
    fun openSettings()
    fun grantActivityRecognition()
}