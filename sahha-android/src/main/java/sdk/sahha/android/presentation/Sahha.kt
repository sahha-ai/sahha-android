package sdk.sahha.android.presentation

import androidx.activity.ComponentActivity
import sdk.sahha.android.di.ManualDependencies

object Sahha {
    private lateinit var manualDependencies: ManualDependencies

    fun configure(activity: ComponentActivity) {
        manualDependencies = ManualDependencies(activity)
    }

    fun authenticate(customerId: String, profileId: String) {
        manualDependencies.authenticateUseCase(customerId, profileId)
    }

    fun startDataCollectionService() {
        manualDependencies.startDataCollectionServiceUseCase()
    }

    fun setPermissionLogic(logic: ((enabled: Boolean) -> Unit)) {
        manualDependencies.setPermissionLogicUseCase(logic)
    }

    fun grantActivityRecognitionPermission() {
        manualDependencies.grantActivityRecognitionPermissionUseCase()
    }

    fun openSettings() {
        manualDependencies.openSettingsUseCase()
    }
}