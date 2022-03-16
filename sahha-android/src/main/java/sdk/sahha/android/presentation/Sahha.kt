package sdk.sahha.android.presentation

import androidx.activity.ComponentActivity
import sdk.sahha.android.di.ManualDependencies
import sdk.sahha.android.domain.model.categories.ActivityRecognition

object Sahha {
    private lateinit var manualDependencies: ManualDependencies
    val activityRecognition by lazy {
        ActivityRecognition(
            manualDependencies.setPermissionLogicUseCase,
            manualDependencies.grantActivityRecognitionPermissionUseCase
        )
    }

    fun configure(activity: ComponentActivity) {
        manualDependencies = ManualDependencies(activity)
        activityRecognition.setPermissionLogic()
    }

    fun authenticate(customerId: String, profileId: String) {
        manualDependencies.authenticateUseCase(customerId, profileId)
    }

    fun startDataCollectionService() {
        manualDependencies.startDataCollectionServiceUseCase()
    }

    fun openAppSettings() {
        manualDependencies.openSettingsUseCase()
    }
}