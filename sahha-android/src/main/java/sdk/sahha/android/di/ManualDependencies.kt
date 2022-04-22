package sdk.sahha.android.di

import android.content.Context
import android.os.Build
import sdk.sahha.android.common.AppCenterLog
import sdk.sahha.android.common.SahhaNotificationManager
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.domain.use_case.*
import sdk.sahha.android.domain.use_case.post.PostAllSensorDataUseCase
import sdk.sahha.android.domain.use_case.post.PostDemographicUseCase
import sdk.sahha.android.domain.use_case.post.PostDeviceDataUseCase
import sdk.sahha.android.domain.use_case.post.PostSleepDataUseCase
import sdk.sahha.android.source.SahhaEnvironment
import javax.inject.Inject

class ManualDependencies @Inject constructor(
    internal val context: Context,
    internal val environment: Enum<SahhaEnvironment>
) {
    internal val api by lazy { AppModule.provideSahhaApi(environment) }

    internal val database by lazy { AppModule.provideDatabase(context) }
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
            context,
            encryptor,
            appCenterLog
        )
    }
    internal val backgroundRepo by lazy {
        AppModule.provideBackgroundRepository(
            context,
            defaultScope,
            ioScope,
            configurationDao,
            api
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

    val notifications by lazy { SahhaNotificationManager(context, backgroundRepo) }
    val timeManager by lazy { getSahhaTimeManager() }
    val encryptor by lazy { Encryptor(securityDao) }
    val decryptor by lazy { Decryptor(securityDao) }
    val appCenterLog by lazy { AppCenterLog(context, configurationDao, defaultScope) }

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
    val analyzeProfileUseCase by lazy { AnalyzeProfileUseCase(remotePostRepo) }
    val getDemographicUseCase by lazy { GetDemographicUseCase(remotePostRepo) }
    val postDemographicUseCase by lazy { PostDemographicUseCase(remotePostRepo) }
    val postAllSensorDataUseCase by lazy { PostAllSensorDataUseCase(remotePostRepo) }

    private fun getSahhaTimeManager(): SahhaTimeManager? {
        if (Build.VERSION.SDK_INT < 24) return null
        return SahhaTimeManager()
    }
}