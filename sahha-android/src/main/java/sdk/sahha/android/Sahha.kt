package sdk.sahha.android

import androidx.activity.ComponentActivity
import androidx.annotation.Keep
import sdk.sahha.android.di.ManualDependencies
import sdk.sahha.android.domain.model.enums.ActivityStatus

@Keep
object Sahha {
    private lateinit var manualDependencies: ManualDependencies

    fun configure(activity: ComponentActivity) {
        manualDependencies = ManualDependencies(activity)
        manualDependencies.setPermissionLogicUseCase()
    }

    fun activate(callback: ((activityStatus: Enum<ActivityStatus>) -> Unit)) {
        manualDependencies.activateUseCase(callback)
    }

    fun authenticate(customerId: String, profileId: String, callback: ((value: String) -> Unit)) {
        manualDependencies.authenticateUseCase(customerId, profileId, callback)
    }

    fun startDataCollectionService() {
        manualDependencies.startDataCollectionServiceUseCase()
    }

    fun promptUserToActivate(callback: (activityStatus: Enum<ActivityStatus>) -> Unit) {
        manualDependencies.promptUserToActivateUseCase(callback)
    }
}