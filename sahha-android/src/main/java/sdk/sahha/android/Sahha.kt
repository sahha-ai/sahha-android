package sdk.sahha.android

import androidx.activity.ComponentActivity
import androidx.annotation.Keep
import sdk.sahha.android.di.ManualDependencies
import sdk.sahha.android.domain.model.enums.ActivityStatus

@Keep
object Sahha {
    internal lateinit var di: ManualDependencies

    internal val notifications by lazy { di.notifications }
    val timeManager by lazy { di.timeManager }

    fun configure(activity: ComponentActivity) {
        di = ManualDependencies(activity)
        di.setPermissionLogicUseCase()
    }

    fun activate(callback: ((activityStatus: Enum<ActivityStatus>) -> Unit)) {
        di.activateUseCase(callback)
    }

    fun authenticate(customerId: String, profileId: String, callback: ((value: String) -> Unit)) {
        di.authenticateUseCase(customerId, profileId, callback)
    }

    fun startDataCollectionService(
        icon: Int? = null,
        title: String? = null,
        shortDescription: String? = null
    ) {
        di.startDataCollectionServiceUseCase(icon, title, shortDescription)
    }

    fun promptUserToActivate(callback: (activityStatus: Enum<ActivityStatus>) -> Unit) {
        di.promptUserToActivateUseCase(callback)
    }
}