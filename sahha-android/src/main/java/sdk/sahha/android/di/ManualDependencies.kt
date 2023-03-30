package sdk.sahha.android.di

import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.os.PowerManager
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.data.local.SahhaDatabase
import sdk.sahha.android.domain.manager.ReceiverManager
import sdk.sahha.android.domain.manager.SahhaNotificationManager
import sdk.sahha.android.domain.repository.SensorRepo
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
    internal lateinit var sensorRepo: SensorRepo
    internal lateinit var sahhaErrorLogger: SahhaErrorLogger
    internal lateinit var powerManager: PowerManager
    internal lateinit var keyguardManager: KeyguardManager
    internal lateinit var sensorManager: SensorManager
    internal lateinit var notificationManager: SahhaNotificationManager
    internal lateinit var receiverManager: ReceiverManager
    internal lateinit var encryptedSharedPreferences: SharedPreferences

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
            api,
            encryptedSharedPreferences,
        )
    }

    internal val deviceInfoRepo by lazy {
        AppModule.provideDeviceInfoRepo(
            authRepo,
            api,
            sahhaErrorLogger
        )
    }

    internal val userDataRepo by lazy {
        AppModule.provideUserDataRepo(
            mainScope,
            authRepo,
            api,
            sahhaErrorLogger
        )
    }

    internal val mainScope by lazy { AppModule.provideMainScope() }
    internal val ioScope by lazy { AppModule.provideIoScope() }
    internal val defaultScope by lazy { AppModule.provideDefaultScope() }

    val timeManager by lazy { getSahhaTimeManager() }
    val encryptor by lazy { Encryptor(securityDao) }
    val decryptor by lazy { Decryptor(securityDao) }
    val mutex by lazy { Mutex() }

    val saveTokensUseCase by lazy { SaveTokensUseCase(authRepo) }
    val startDataCollectionServiceUseCase by lazy {
        StartDataCollectionServiceUseCase(
            notificationManager
        )
    }
    val postStepDataUseCase by lazy { PostStepDataUseCase(sensorRepo) }
    val postSleepDataUseCase by lazy { PostSleepDataUseCase(sensorRepo) }
    val postDeviceDataUseCase by lazy { PostDeviceDataUseCase(sensorRepo) }
    val startCollectingSleepDataUseCase by lazy { StartCollectingSleepDataUseCase(sensorRepo) }
    val startPostWorkersUseCase by lazy { StartPostWorkersUseCase(sensorRepo) }
    val startCollectingPhoneScreenLockDataUseCase by lazy {
        StartCollectingPhoneScreenLockDataUseCase(
            receiverManager
        )
    }
    val startCollectingStepCounterData by lazy { StartCollectingStepCounterData(sensorRepo) }
    val startCollectingStepDetectorData by lazy { StartCollectingStepDetectorData(sensorRepo) }
    val analyzeProfileUseCase by lazy {
        AnalyzeProfileUseCase(
            userDataRepo,
            timeManager,
            sahhaErrorLogger
        )
    }
    val getDemographicUseCase by lazy { GetDemographicUseCase(userDataRepo) }
    val postDemographicUseCase by lazy { PostDemographicUseCase(userDataRepo) }
    val postAllSensorDataUseCase by lazy { PostAllSensorDataUseCase(sensorRepo) }

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
    val getSensorDataUseCase by lazy { GetSensorDataUseCase(sensorRepo) }

    fun setDependencies(context: Context) {
        setDatabase(context)
        setEncryptedSharedPreferences(context)
        setSahhaErrorLogger(context)

        runBlocking {
            listOf(
                async { setSensorRepository(context) },
                async { setReceiverManager(context) },
                async { setNotificationManager(context) },
                async { setPowerManager(context) },
                async { setKeyguardManager(context) },
                async { setSensorManager(context) },
            ).joinAll()
        }
    }

    private fun setSensorManager(context: Context) {
        if (!::sensorManager.isInitialized)
            sensorManager = AppModule.provideSensorManager(context)
    }

    private fun setPowerManager(context: Context) {
        if (!::powerManager.isInitialized)
            powerManager = AppModule.providePowerManager(context)
    }

    private fun setKeyguardManager(context: Context) {
        if (!::keyguardManager.isInitialized)
            keyguardManager = AppModule.provideKeyguardManager(context)
    }

    internal fun setDatabase(context: Context) {
        if (!::database.isInitialized)
            database = AppModule.provideDatabase(context)
    }

    private fun setSensorRepository(context: Context) {
        if (!::sensorRepo.isInitialized)
            sensorRepo = AppModule.provideSensorRepository(
                context,
                mainScope,
                configurationDao,
                deviceUsageDao,
                sleepDao,
                movementDao,
                authRepo,
                sahhaErrorLogger,
                mutex,
                api
            )
    }

    private fun setReceiverManager(context: Context) {
        if (!::receiverManager.isInitialized)
            receiverManager = AppModule.provideReceiverManager(context, mainScope)
    }

    private fun setNotificationManager(context: Context) {
        if (!::notificationManager.isInitialized)
            notificationManager = AppModule.provideSahhaNotificationManager(context)
    }

    private fun setEncryptedSharedPreferences(context: Context) {
        if (!::encryptedSharedPreferences.isInitialized)
            encryptedSharedPreferences = AppModule.provideEncryptedSharedPreferences(context)
    }

    private fun setSahhaErrorLogger(context: Context) {
        if (!::sahhaErrorLogger.isInitialized)
            sahhaErrorLogger =
                AppModule.provideSahhaErrorLogger(
                    context,
                    configurationDao,
                    sahhaErrorApi,
                    defaultScope,
                    authRepo
                )
    }

    private fun getSahhaTimeManager(): SahhaTimeManager {
        return SahhaTimeManager()
    }
}