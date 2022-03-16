package sdk.sahha.android.domain.repository

import sdk.sahha.android.domain.model.Logic

interface PermissionsRepo {
    fun setPermissionLogic()
    fun setPermissionLogic(logic: Logic)
    fun openSettings()
    fun grantActivityRecognition()
}