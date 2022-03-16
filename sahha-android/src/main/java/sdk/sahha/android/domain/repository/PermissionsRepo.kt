package sdk.sahha.android.domain.repository

interface PermissionsRepo {
    fun setPermissionLogic()
    fun setPermissionLogic(logic: ((enabled: Boolean) -> Unit))
    fun openSettings()
    fun grantActivityRecognition()
}