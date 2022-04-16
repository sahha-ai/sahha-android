package sdk.sahha.android.di

import androidx.activity.ComponentActivity
import sdk.sahha.android.common.AppCenterLog
import sdk.sahha.android.common.SahhaNotificationManager
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.domain.model.enums.SahhaEnvironment
import sdk.sahha.android.domain.use_case.*
import sdk.sahha.android.domain.use_case.permissions.ActivateUseCase
import sdk.sahha.android.domain.use_case.permissions.PromptUserToActivateUseCase
import sdk.sahha.android.domain.use_case.permissions.SetPermissionLogicUseCase
import sdk.sahha.android.domain.use_case.post.PostDemographicUseCase
import sdk.sahha.android.domain.use_case.post.PostDeviceDataUseCase
import sdk.sahha.android.domain.use_case.post.PostSleepDataUseCase
import javax.inject.Inject

class ManualDependencies @Inject constructor(
    internal val activity: ComponentActivity,
    internal val environment: Enum<SahhaEnvironment>
) {
    internal val api by lazy { AppModule.provideSahhaApi(environment) }

    internal val database by lazy { AppModule.provideDatabase(activity) }
    internal val securityDao by lazy { AppModule.provideSecurityDao(database) }
    internal val movementDao by lazy { AppModule.provideMovementDao(database) }
    internal val sleepDao by lazy { AppModule.provideSleepDao(database) }
    internal val deviceUsageDao by lazy { AppModule.provideDeviceUsageDao(database) }
    internal val configurationDao by lazy { AppModule.provideConfigDao(database) }

    internal val authRepo by lazy {
        AppModule.provideAuthRepository(
            api,
            ioScope,
            mainScope,
            activity,
            encryptor
        )
    }
    internal val backgroundRepo by lazy {
        AppModule.provideBackgroundRepository(
            activity,
            defaultScope,
            ioScope,
            configurationDao,
            api
        )
    }
    internal val permissionRepo by lazy {
        AppModule.providePermissionsRepository(
            activity
        )
    }
    internal val remotePostRepo by lazy {
        AppModule.provideRemotePostRepository(
            ioScope,
            sleepDao,
            deviceUsageDao,
            encryptor,
            decryptor,
            api,
            appCenterLog
        )
    }

    internal val mainScope by lazy { AppModule.provideMainScope() }
    internal val ioScope by lazy { AppModule.provideIoScope() }
    internal val defaultScope by lazy { AppModule.provideDefaultScope() }

    val notifications by lazy { SahhaNotificationManager(activity, backgroundRepo) }
    val timeManager by lazy { SahhaTimeManager() }
    val encryptor by lazy { Encryptor(securityDao) }
    val decryptor by lazy { Decryptor(securityDao) }
    val appCenterLog by lazy { AppCenterLog(activity, configurationDao, defaultScope) }

    val saveTokensUseCase by lazy { SaveTokensUseCase(authRepo) }
    val startDataCollectionServiceUseCase by lazy {
        StartDataCollectionServiceUseCase(
            backgroundRepo
        )
    }
    val activateUseCase by lazy {
        ActivateUseCase(
            permissionRepo
        )
    }
    val promptUserToActivateUseCase by lazy { PromptUserToActivateUseCase(permissionRepo) }
    val setPermissionLogicUseCase by lazy { SetPermissionLogicUseCase(permissionRepo) }
    val postSleepDataUseCase by lazy { PostSleepDataUseCase(remotePostRepo) }
    val postDeviceDataUseCase by lazy { PostDeviceDataUseCase(remotePostRepo) }
    val startCollectingSleepDataUseCase by lazy { StartCollectingSleepDataUseCase(backgroundRepo) }
    val startPostWorkersUseCase by lazy { StartPostWorkersUseCase(backgroundRepo) }
    val startCollectingPhoneScreenLockDataUseCase by lazy {
        StartCollectingPhoneScreenLockDataUseCase(
            backgroundRepo
        )
    }
    val analyzeProfileUseCase by lazy { AnalyzeProfileUseCase(remotePostRepo) }
    val getDemographicUseCase by lazy { GetDemographicUseCase(remotePostRepo) }
    val postDemographicUseCase by lazy { PostDemographicUseCase(remotePostRepo) }
}