package sdk.sahha.android.domain.repository

interface PermissionsRepo {
    fun setPermissionLogic(enabledLogic: (() -> Unit), disabledLogic: (() -> Unit))
    fun openSettings()
    fun grantActivityRecognition()
}