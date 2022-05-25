package sdk.sahha.android.di

import android.content.Context
import android.os.Build
import sdk.sahha.android.common.AppCenterLog
import sdk.sahha.android.common.SahhaNotificationManager
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.data.local.SahhaDatabase
import sdk.sahha.android.domain.repository.BackgroundRepo
import sdk.sahha.android.domain.use_case.*
import sdk.sahha.android.domain.use_case.permissions.ActivateUseCase
import sdk.sahha.android.domain.use_case.permissions.OpenAppSettingsUseCase
import sdk.sahha.android.domain.use_case.permissions.SetPermissionLogicUseCase
import sdk.sahha.android.domain.use_case.post.PostAllSensorDataUseCase
import sdk.sahha.android.domain.use_case.post.PostDemographicUseCase
import sdk.sahha.android.domain.use_case.post.PostDeviceDataUseCase
import sdk.sahha.android.domain.use_case.post.PostSleepDataUseCase
import sdk.sahha.android.source.SahhaEnvironment
import javax.inject.Inject

class ManualDependencies @Inject constructor(
    internal val environment: Enum<SahhaEnvironment>
) {
    internal lateinit var database: SahhaDatabase
    internal lateinit var backgroundRepo: BackgroundRepo
    internal lateinit var notifications: SahhaNotificationManager
    internal lateinit var appCenterLog: AppCenterLog

    internal val api by lazy { AppModule.provideSahhaApi(environment) }
    internal val securityDao by lazy { AppModule.provideSecurityDao(database) }
    internal val movementDao by lazy { AppModule.provideMovementDao(database) }
    internal val sleepDao by lazy { AppModule.provideSleepDao(database) }
    internal val deviceUsageDao by lazy { AppModule.provideDeviceUsageDao(database) }
    internal val configurationDao by lazy { AppModule.provideConfigDao(database) }

    internal val authRepo by lazy {
        AppModule.provideAuthRepository(
            encryptor,
            appCenterLog
        )
    }
    internal val remotePostRepo by lazy {
        AppModule.provideRemotePostRepository(
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

    val timeManager by lazy { getSahhaTimeManager() }
    val encryptor by lazy { Encryptor(securityDao) }
    val decryptor by lazy { Decryptor(securityDao) }


    val saveTokensUseCase by lazy { SaveTokensUseCase(authRepo) }
    val startDataCollectionServiceUseCase by lazy {
        StartDataCollectionServiceUseCase(
            backgroundRepo
        )
    }
    val postSleepDataUseCase by lazy { PostSleepDataUseCase(remotePostRepo) }
    val postDeviceDataUseCase by lazy { PostDeviceDataUseCase(remotePostRepo) }
    val startCollectingSleepDataUseCase by lazy { StartCollectingSleepDataUseCase(backgroundRepo) }
    val startPostWorkersUseCase by lazy { StartPostWorkersUseCase(backgroundRepo) }
    val startCollectingPhoneScreenLockDataUseCase by lazy {
        StartCollectingPhoneScreenLockDataUseCase(
            backgroundRepo
        )
    }
    val analyzeProfileUseCase by lazy { AnalyzeProfileUseCase(remotePostRepo, timeManager) }
    val getDemographicUseCase by lazy { GetDemographicUseCase(remotePostRepo) }
    val postDemographicUseCase by lazy { PostDemographicUseCase(remotePostRepo) }
    val postAllSensorDataUseCase by lazy { PostAllSensorDataUseCase(remotePostRepo) }

    val permissionRepo by lazy {
        AppModule.providePermissionsRepository()
    }
    val activateUseCase by lazy {
        ActivateUseCase(
            permissionRepo
        )
    }
    val openAppSettingsUseCase by lazy { OpenAppSettingsUseCase(permissionRepo) }
    val setPermissionLogicUseCase by lazy { SetPermissionLogicUseCase(permissionRepo) }

    fun setDependencies(context: Context) {
        setDatabase(context)
        setBackgroundRepo(context)
        setNotifications(context)
        setAppCenterLog(context)
    }

    private fun setDatabase(context: Context) {
        database = AppModule.provideDatabase(context)
    }

    private fun setBackgroundRepo(context: Context) {
        backgroundRepo = AppModule.provideBackgroundRepository(
            context,
            defaultScope,
            ioScope,
            configurationDao,
        )
    }

    private fun setNotifications(context: Context) {
        notifications = SahhaNotificationManager(context, backgroundRepo)
    }

    private fun setAppCenterLog(context: Context) {
        appCenterLog = AppCenterLog(context, configurationDao, defaultScope)
    }

    private fun getSahhaTimeManager(): SahhaTimeManager? {
        if (Build.VERSION.SDK_INT < 24) return null
        return SahhaTimeManager()
    }
}