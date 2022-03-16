package sdk.sahha.android.presentation

import androidx.activity.ComponentActivity
import sdk.sahha.android.di.ManualDependencies
import sdk.sahha.android.domain.model.Logic

object Sahha {
    private lateinit var manualDependencies: ManualDependencies
    private val logic = Logic()

    fun configure(activity: ComponentActivity) {
        manualDependencies = ManualDependencies(activity)
        setPermissionLogic(logic)
    }

    fun authenticate(customerId: String, profileId: String) {
        manualDependencies.authenticateUseCase(customerId, profileId)
    }

    fun startDataCollectionService() {
        manualDependencies.startDataCollectionServiceUseCase()
    }

    fun setPermissionLogic(logic: Logic) {
        manualDependencies.setPermissionLogicUseCase(logic)
    }

    fun grantActivityRecognitionPermission(permissionLogic: ((enabled: Boolean) -> Unit)) {
        logic.unit = permissionLogic
        manualDependencies.grantActivityRecognitionPermissionUseCase()
    }

    fun openSettings() {
        manualDependencies.openSettingsUseCase()
    }
}