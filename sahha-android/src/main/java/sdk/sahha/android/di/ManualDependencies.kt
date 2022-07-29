package sdk.sahha.android.di

import android.app.KeyguardManager
import android.content.Context
import android.hardware.SensorManager
import android.os.PowerManager
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaNotificationManager
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.data.local.SahhaDatabase
import sdk.sahha.android.domain.repository.BackgroundRepo
import sdk.sahha.android.domain.use_case.AnalyzeProfileUseCase
import sdk.sahha.android.domain.use_case.GetDemographicUseCase
import sdk.sahha.android.domain.use_case.GetSensorDataUseCase
import sdk.sahha.android.domain.use_case.SaveTokensUseCase
import sdk.sahha.android.domain.use_case.background.*
import sdk.sahha.android.domain.use_case.permissions.ActivateUseCase
import sdk.sahha.android.domain.use_case.permissions.OpenAppSettingsUseCase
import sdk.sahha.android.domain.use_case.permissions.SetPermissionLogicUseCase
import sdk.sahha.android.domain.use_case.post.*
import sdk.sahha.android.source.SahhaEnvironment

class ManualDependencies(
    internal val environment: Enum<SahhaEnvironment>
) {
    internal lateinit var database: SahhaDatabase
    internal lateinit var backgroundRepo: BackgroundRepo
    internal lateinit var notifications: SahhaNotificationManager
    internal lateinit var sahhaErrorLogger: SahhaErrorLogger
    internal lateinit var powerManager: PowerManager
    internal lateinit var keyguardManager: KeyguardManager
    internal lateinit var sensorManager: SensorManager

    internal val gson by lazy { AppModule.provideGsonConverter() }
    internal val api by lazy { AppModule.provideSahhaApi(environment, gson) }
    internal val sahhaErrorApi by lazy { AppModule.provideSahhaErrorApi(environment, gson) }
    internal val securityDao by lazy { AppModule.provideSecurityDao(database) }
    internal val movementDao by lazy { AppModule.provideMovementDao(database) }
    internal val sleepDao by lazy { AppModule.provideSleepDao(database) }
    internal val deviceUsageDao by lazy { AppModule.provideDeviceUsageDao(database) }
    internal val configurationDao by lazy { AppModule.provideConfigDao(database) }

    internal val authRepo by lazy {
        AppModule.provideAuthRepository(
            encryptor,
            sahhaErrorLogger
        )
    }
    internal val remotePostRepo by lazy {
        AppModule.provideRemotePostRepository(
            sleepDao,
            deviceUsageDao,
            movementDao,
            encryptor,
            decryptor,
            api,
            sahhaErrorLogger,
            ioScope
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
    val postStepDataUseCase by lazy { PostStepDataUseCase(remotePostRepo) }
    val postSleepDataUseCase by lazy { PostSleepDataUseCase(remotePostRepo) }
    val postDeviceDataUseCase by lazy { PostDeviceDataUseCase(remotePostRepo) }
    val startCollectingSleepDataUseCase by lazy { StartCollectingSleepDataUseCase(backgroundRepo) }
    val startPostWorkersUseCase by lazy { StartPostWorkersUseCase(backgroundRepo) }
    val startCollectingPhoneScreenLockDataUseCase by lazy {
        StartCollectingPhoneScreenLockDataUseCase(
            backgroundRepo
        )
    }
    val startCollectingStepCounterData by lazy { StartCollectingStepCounterData(backgroundRepo) }
    val startCollectingStepDetectorData by lazy { StartCollectingStepDetectorData(backgroundRepo) }
    val analyzeProfileUseCase by lazy {
        AnalyzeProfileUseCase(
            remotePostRepo,
            timeManager,
            sahhaErrorLogger
        )
    }
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
    val getSensorDataUseCase by lazy { GetSensorDataUseCase(backgroundRepo) }

    fun setDependencies(context: Context) {
        setDatabase(context)
        setBackgroundRepo(context)
        setNotifications(context)

        mainScope.launch {
            listOf(
                async { setSahhaErrorLogger(context) },
                async { setPowerManager(context) },
                async { setKeyguardManager(context) },
                async { setSensorManager(context) },
            ).joinAll()
        }
    }

    private fun setSensorManager(context: Context) {
        sensorManager = AppModule.provideSensorManager(context)
    }

    private fun setPowerManager(context: Context) {
        powerManager = AppModule.providePowerManager(context)
    }

    private fun setKeyguardManager(context: Context) {
        keyguardManager = AppModule.provideKeyguardManager(context)
    }

    private fun setDatabase(context: Context) {
        database = AppModule.provideDatabase(context)
    }

    private fun setBackgroundRepo(context: Context) {
        backgroundRepo = AppModule.provideBackgroundRepository(
            context,
            mainScope,
            configurationDao,
            deviceUsageDao,
            sleepDao,
            movementDao
        )
    }

    private fun setNotifications(context: Context) {
        notifications = SahhaNotificationManager(context, backgroundRepo)
    }

    private fun setSahhaErrorLogger(context: Context) {
        sahhaErrorLogger =
            SahhaErrorLogger(context, configurationDao, decryptor, sahhaErrorApi, defaultScope)
    }

    private fun getSahhaTimeManager(): SahhaTimeManager {
        return SahhaTimeManager()
    }
}