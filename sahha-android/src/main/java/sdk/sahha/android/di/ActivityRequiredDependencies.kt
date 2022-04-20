package sdk.sahha.android.di

import androidx.activity.ComponentActivity
import sdk.sahha.android.domain.use_case.permissions.ActivateUseCase
import sdk.sahha.android.domain.use_case.permissions.PromptUserToActivateUseCase
import sdk.sahha.android.domain.use_case.permissions.SetPermissionLogicUseCase

class ActivityRequiredDependencies(
    private val activity: ComponentActivity
) {
    val permissionRepo by lazy {
        AppModule.providePermissionsRepository(activity)
    }
    val activateUseCase by lazy {
        ActivateUseCase(
            permissionRepo
        )
    }
    val promptUserToActivateUseCase by lazy { PromptUserToActivateUseCase(permissionRepo) }
    val setPermissionLogicUseCase by lazy { SetPermissionLogicUseCase(permissionRepo) }
}