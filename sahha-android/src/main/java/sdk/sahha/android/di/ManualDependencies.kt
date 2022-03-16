package sdk.sahha.android.di

import androidx.activity.ComponentActivity
import sdk.sahha.android.domain.use_case.AuthenticateUseCase
import sdk.sahha.android.domain.use_case.permissions.GrantActivityRecognitionPermissionUseCase
import sdk.sahha.android.domain.use_case.permissions.OpenSettingsUseCase
import sdk.sahha.android.domain.use_case.StartDataCollectionServiceUseCase
import sdk.sahha.android.domain.use_case.permissions.SetPermissionLogicUseCase
import javax.inject.Inject

class ManualDependencies @Inject constructor(
    private val activity: ComponentActivity
) {
    private val api by lazy { AppModule.provideSahhaApi() }

    private val database by lazy { AppModule.provideDatabase(activity) }
    private val securityDao by lazy { AppModule.provideSecurityDao(database) }
    private val movementDao by lazy { AppModule.provideMovementDao(database) }

    private val authRepo by lazy {
        AppModule.provideAuthRepository(
            api,
            ioScope,
            activity,
            securityDao
        )
    }
    private val backgroundRepo by lazy {
        AppModule.provideBackgroundRepository(
            activity,
            defaultScope
        )
    }
    private val permissionRepo by lazy {
        AppModule.providePermissionsRepository(
            activity
        )
    }

    private val ioScope by lazy { AppModule.provideIoScope() }
    private val defaultScope by lazy { AppModule.provideDefaultScope() }

    val authenticateUseCase by lazy { AuthenticateUseCase(authRepo) }
    val startDataCollectionServiceUseCase by lazy { StartDataCollectionServiceUseCase(backgroundRepo) }
    val grantActivityRecognitionPermissionUseCase by lazy {
        GrantActivityRecognitionPermissionUseCase(
            permissionRepo
        )
    }
    val openSettingsUseCase by lazy { OpenSettingsUseCase(permissionRepo) }
    val setPermissionLogicUseCase by lazy { SetPermissionLogicUseCase(permissionRepo) }
}