package sdk.sahha.android.di

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.os.PowerManager
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.sync.Mutex
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sdk.sahha.android.BuildConfig
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.data.local.SahhaDatabase
import sdk.sahha.android.data.local.SahhaDbUtility
import sdk.sahha.android.data.local.dao.BatchedDataDao
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.data.local.dao.DeviceUsageDao
import sdk.sahha.android.data.local.dao.HealthConnectConfigDao
import sdk.sahha.android.data.local.dao.ManualPermissionsDao
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.data.local.dao.SecurityDao
import sdk.sahha.android.data.local.dao.SleepDao
import sdk.sahha.android.data.manager.IdManagerImpl
import sdk.sahha.android.data.manager.PermissionManagerImpl
import sdk.sahha.android.data.manager.PostChunkManagerImpl
import sdk.sahha.android.data.mapper.HealthConnectMapperDefaults
import sdk.sahha.android.data.provider.PermissionActionProviderImpl
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.remote.SahhaErrorApi
import sdk.sahha.android.data.repository.AppCrashRepoImpl
import sdk.sahha.android.data.repository.AuthRepoImpl
import sdk.sahha.android.data.repository.BatchedDataRepoImpl
import sdk.sahha.android.data.repository.DeviceInfoRepoImpl
import sdk.sahha.android.data.repository.DeviceUsageRepoImpl
import sdk.sahha.android.data.repository.HealthConnectRepoImpl
import sdk.sahha.android.data.repository.InsightsRepoImpl
import sdk.sahha.android.data.repository.SahhaConfigRepoImpl
import sdk.sahha.android.data.repository.SensorRepoImpl
import sdk.sahha.android.data.repository.SleepRepoImpl
import sdk.sahha.android.data.repository.UserDataRepoImpl
import sdk.sahha.android.domain.interaction.PermissionInteractionManager
import sdk.sahha.android.domain.interaction.SensorInteractionManager
import sdk.sahha.android.domain.manager.ConnectionStateManager
import sdk.sahha.android.domain.manager.IdManager
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.manager.PostChunkManager
import sdk.sahha.android.domain.manager.ReceiverManager
import sdk.sahha.android.domain.manager.SahhaNotificationManager
import sdk.sahha.android.domain.mapper.AggregationDataTypeMapper
import sdk.sahha.android.domain.mapper.HealthConnectConstantsMapper
import sdk.sahha.android.domain.model.callbacks.ActivityCallback
import sdk.sahha.android.domain.model.categories.PermissionHandler
import sdk.sahha.android.domain.model.processor.AppEventProcessor
import sdk.sahha.android.domain.provider.PermissionActionProvider
import sdk.sahha.android.domain.repository.AppCrashRepo
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.BatchedDataRepo
import sdk.sahha.android.domain.repository.DeviceInfoRepo
import sdk.sahha.android.domain.repository.DeviceUsageRepo
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.domain.repository.InsightsRepo
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.repository.SleepRepo
import sdk.sahha.android.domain.repository.UserDataRepo
import sdk.sahha.android.domain.transformer.AggregateDataLogTransformer
import sdk.sahha.android.domain.use_case.CalculateBatchLimit
import sdk.sahha.android.domain.use_case.background.BatchAggregateLogs
import sdk.sahha.android.domain.use_case.background.LogAppEvent
import sdk.sahha.android.framework.manager.AndroidConnectionStateManager
import sdk.sahha.android.framework.manager.ReceiverManagerImpl
import sdk.sahha.android.framework.manager.SahhaNotificationManagerImpl
import sdk.sahha.android.framework.mapper.HealthConnectConstantsMapperImpl
import sdk.sahha.android.framework.mapper.SensorToHealthConnectMetricMapper
import sdk.sahha.android.framework.observer.HostAppLifecycleObserver
import sdk.sahha.android.framework.runnable.DataBatcherRunnable
import sdk.sahha.android.framework.processor.AppEventProcessorImpl
import sdk.sahha.android.source.SahhaEnvironment
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

private const val tag = "AppModule"

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MainScope

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoScope

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultScope

@Module
internal class AppModule(private val sahhaEnvironment: Enum<SahhaEnvironment>) {
    @Singleton
    @Provides
    fun provideSahhaEnvironment(): Enum<SahhaEnvironment> {
        return sahhaEnvironment
    }

    @Singleton
    @Provides
    fun provideUserDataRepo(
        @IoScope ioScope: CoroutineScope,
        authRepo: AuthRepo,
        api: SahhaApi,
        sahhaErrorLogger: SahhaErrorLogger
    ): UserDataRepo {
        return UserDataRepoImpl(
            ioScope,
            authRepo,
            api,
            sahhaErrorLogger
        )
    }

    @Singleton
    @Provides
    fun provideSahhaConfigRepo(
        configDao: ConfigurationDao
    ): SahhaConfigRepo {
        return SahhaConfigRepoImpl(
            configDao
        )
    }

    @Singleton
    @Provides
    fun provideReceiverManager(
        context: Context,
        @MainScope mainScope: CoroutineScope
    ): ReceiverManager {
        return ReceiverManagerImpl(
            context,
            mainScope
        )
    }

    @Singleton
    @Provides
    fun provideIdManager(
        sharedPreferences: SharedPreferences
    ): IdManager {
        return IdManagerImpl(sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideDeviceInfoRepo(
        configDao: ConfigurationDao,
        api: SahhaApi,
        sahhaErrorLogger: SahhaErrorLogger,
        idManager: IdManager,
    ): DeviceInfoRepo {
        return DeviceInfoRepoImpl(
            configDao,
            api,
            sahhaErrorLogger,
            idManager
        )
    }

    @Singleton
    @Provides
    fun provideSahhaNotificationManager(
        context: Context,
        sahhaErrorLogger: SahhaErrorLogger,
        notificationManager: NotificationManager,
        configRepo: SahhaConfigRepo,
        @DefaultScope defaultScope: CoroutineScope,
    ): SahhaNotificationManager {
        return SahhaNotificationManagerImpl(
            context,
            sahhaErrorLogger,
            notificationManager,
            configRepo,
            defaultScope
        )
    }

    @Singleton
    @Provides
    fun provideEncryptedSharedPreferences(context: Context): SharedPreferences {
        return try {
            createEncryptedSharedPreferences(context)
        } catch (e: Exception) {
            Log.w(tag, e.message, e)
            context.deleteSharedPreferences("encrypted_prefs")
            createEncryptedSharedPreferences(context)
        }
    }

    private fun createEncryptedSharedPreferences(context: Context): SharedPreferences {
        val masterKeyAlias =
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

        return EncryptedSharedPreferences.create(
            context,
            "encrypted_prefs",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Singleton
    @Provides
    fun providePowerManager(
        context: Context
    ): PowerManager {
        return context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    @Singleton
    @Provides
    fun provideKeyguardManager(
        context: Context
    ): KeyguardManager {
        return context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    }

    @Singleton
    @Provides
    fun provideGsonConverter(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    @Singleton
    @Provides
    fun provideSahhaApi(
        context: Context,
        environment: Enum<SahhaEnvironment>,
        gson: GsonConverterFactory,
        okHttpClient: OkHttpClient
    ): SahhaApi {
        return detectApiBaseUrl(
            context,
            environment,
            gson,
            okHttpClient,
            SahhaApi::class.java
        )
    }

    @Singleton
    @Provides
    fun provideSahhaErrorApi(
        context: Context,
        environment: Enum<SahhaEnvironment>,
        gson: GsonConverterFactory,
        okHttpClient: OkHttpClient
    ): SahhaErrorApi {
        return detectApiBaseUrl(
            context,
            environment,
            gson,
            okHttpClient,
            SahhaErrorApi::class.java
        )
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(Constants.OKHTTP_CLIENT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.OKHTTP_CLIENT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.OKHTTP_CLIENT_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    private fun <T> detectApiBaseUrl(
        context: Context,
        environment: Enum<SahhaEnvironment>,
        gson: GsonConverterFactory,
        okHttpClient: OkHttpClient,
        apiClass: Class<T>
    ): T {
        return if (environment == SahhaEnvironment.production) {
            Retrofit.Builder()
                .baseUrl(BuildConfig.API_PROD)
                .client(okHttpClient)
                .addConverterFactory(gson)
                .build()
                .create(apiClass)
        } else if (environment == SahhaEnvironment.development) {
            Retrofit.Builder()
                .baseUrl(BuildConfig.API_DEV)
                .client(okHttpClient)
                .addConverterFactory(gson)
                .build()
                .create(apiClass)
        } else {
            Retrofit.Builder()
                .baseUrl(BuildConfig.API_SANDBOX)
                .client(okHttpClient)
                .addConverterFactory(gson)
                .build()
                .create(apiClass)
        }
    }

    @Singleton
    @Provides
    fun provideAuthRepository(
        api: SahhaApi,
        encryptedSharedPreferences: SharedPreferences,
    ): AuthRepo {
        return AuthRepoImpl(
            api,
            encryptedSharedPreferences
        )
    }

    @Singleton
    @Provides
    fun provideSensorRepository(
        context: Context,
        @DefaultScope defaultScope: CoroutineScope,
        @IoScope ioScope: CoroutineScope,
        sahhaConfigRepo: SahhaConfigRepo,
        deviceUsageRepo: DeviceUsageRepo,
        sleepDao: SleepDao,
        movementDao: MovementDao,
        authRepo: AuthRepo,
        sahhaErrorLogger: SahhaErrorLogger,
        mutex: Mutex,
        api: SahhaApi,
        chunkManager: PostChunkManager,
        permissionManager: PermissionManager,
        timeManager: SahhaTimeManager
    ): SensorRepo {
        return SensorRepoImpl(
            context,
            defaultScope,
            ioScope,
            deviceUsageRepo,
            sleepDao,
            movementDao,
            authRepo,
            sahhaConfigRepo,
            sahhaErrorLogger,
            mutex,
            api,
            chunkManager,
            permissionManager,
            timeManager
        )
    }

    @Singleton
    @Provides
    fun provideInsightsRepository(
        timeManager: SahhaTimeManager,
        api: SahhaApi,
        sahhaErrorLogger: SahhaErrorLogger,
        client: HealthConnectClient?,
        @IoScope ioScope: CoroutineScope
    ): InsightsRepo {
        return InsightsRepoImpl(
            timeManager,
            api,
            sahhaErrorLogger,
            client,
            ioScope
        )
    }

    @Singleton
    @Provides
    fun provideBatchedDataRepository(
        batchedDataDao: BatchedDataDao
    ): BatchedDataRepo {
        return BatchedDataRepoImpl(batchedDataDao)
    }

    @Singleton
    @Provides
    fun provideDeviceUsageRepository(
        deviceUsageDao: DeviceUsageDao
    ): DeviceUsageRepo {
        return DeviceUsageRepoImpl(deviceUsageDao)
    }

    @Singleton
    @Provides
    fun provideSleepRepository(
        sleepDao: SleepDao
    ): SleepRepo {
        return SleepRepoImpl(sleepDao)
    }

    @Singleton
    @Provides
    fun providePermissionHandler(
        activityCallback: ActivityCallback
    ): PermissionHandler {
        return PermissionHandler(activityCallback)
    }

    @Singleton
    @Provides
    fun provideActivityCallback(): ActivityCallback {
        return ActivityCallback()
    }

    @Singleton
    @Provides
    fun providePermissionManager(
        permissionHandler: PermissionHandler,
        configRepo: SahhaConfigRepo,
        manualPermissionsDao: ManualPermissionsDao,
        healthConnectClient: HealthConnectClient?,
        sahhaErrorLogger: SahhaErrorLogger,
        sharedPrefs: SharedPreferences,
        @MainScope mainScope: CoroutineScope,
    ): PermissionManager {
        return PermissionManagerImpl(
            mainScope,
            configRepo,
            manualPermissionsDao,
            permissionHandler,
            healthConnectClient,
            sahhaErrorLogger,
            sharedPrefs
        )
    }

    @Singleton
    @Provides
    fun provideDatabase(context: Context): SahhaDatabase {
        return SahhaDbUtility.getDb(context)
    }

    @Singleton
    @Provides
    fun provideMovementDao(db: SahhaDatabase): MovementDao {
        return db.movementDao()
    }

    @Singleton
    @Provides
    fun provideBatchedDataDao(db: SahhaDatabase): BatchedDataDao {
        return db.BatchedDataDao()
    }

    @Singleton
    @Provides
    fun provideSecurityDao(db: SahhaDatabase): SecurityDao {
        return db.securityDao()
    }

    @Singleton
    @Provides
    fun provideSleepDao(db: SahhaDatabase): SleepDao {
        return db.sleepDao()
    }

    @Singleton
    @Provides
    fun provideDeviceUsageDao(db: SahhaDatabase): DeviceUsageDao {
        return db.deviceUsageDao()
    }

    @Singleton
    @Provides
    fun provideConfigDao(db: SahhaDatabase): ConfigurationDao {
        return db.configurationDao()
    }

    @Singleton
    @Provides
    fun provideHealthConfigDao(db: SahhaDatabase): HealthConnectConfigDao {
        return db.healthConnectConfigDao()
    }

    @Singleton
    @Provides
    fun provideManualPermissionsDao(db: SahhaDatabase): ManualPermissionsDao {
        return db.manualPermissionsDao()
    }

    @DefaultScope
    @Provides
    fun provideDefaultScope(): CoroutineScope {
        return CoroutineScope(Default)
    }

    @IoScope
    @Provides
    fun provideIoScope(): CoroutineScope {
        return CoroutineScope(IO)
    }

    @MainScope
    @Provides
    fun provideMainScope(): CoroutineScope {
        return CoroutineScope(Main)
    }

    @Singleton
    @Provides
    fun provideSahhaErrorLogger(
        context: Context,
        sahhaConfigRepo: SahhaConfigRepo,
        sahhaErrorApi: SahhaErrorApi,
        @DefaultScope defaultScope: CoroutineScope,
        authRepo: AuthRepo
    ): SahhaErrorLogger {
        return SahhaErrorLogger(
            context,
            sahhaErrorApi,
            defaultScope,
            authRepo,
            sahhaConfigRepo
        )
    }

    @Singleton
    @Provides
    fun provideSensorManager(
        context: Context,
    ): SensorManager {
        return context.getSystemService(Service.SENSOR_SERVICE) as SensorManager
    }

    @Singleton
    @Provides
    fun provideTimeManager(): SahhaTimeManager {
        return SahhaTimeManager()
    }

    @Singleton
    @Provides
    fun provideEncryptor(
        securityDao: SecurityDao
    ): Encryptor {
        return Encryptor(securityDao)
    }

    @Singleton
    @Provides
    fun provideDecryptor(
        securityDao: SecurityDao
    ): Decryptor {
        return Decryptor(securityDao)
    }

    @Singleton
    @Provides
    fun provideMutex(): Mutex {
        return Mutex()
    }

    @Named("BatchMutex")
    @Singleton
    @Provides
    fun provideBatchMutex(): Mutex {
        return Mutex()
    }

    @Singleton
    @Provides
    fun providePostChunkManager(
        @Named("BatchMutex") mutex: Mutex
    ): PostChunkManager {
        return PostChunkManagerImpl(mutex)
    }

    @Singleton
    @Provides
    fun provideHealthConnectClient(context: Context): HealthConnectClient? {
        return try {
            HealthConnectClient.getOrCreate(context)
        } catch (e: Exception) {
            println("Caught: " + e.message)
            null
        }
    }

    @Singleton
    @Provides
    fun provideHealthConnectRepo(
        context: Context,
        @DefaultScope defaultScope: CoroutineScope,
        @IoScope ioScope: CoroutineScope,
        chunkManager: PostChunkManager,
        configRepo: SahhaConfigRepo,
        authRepo: AuthRepo,
        sensorRepo: SensorRepo,
        workManager: WorkManager,
        api: SahhaApi,
        client: HealthConnectClient?,
        sahhaErrorLogger: SahhaErrorLogger,
        sahhaTimeManager: SahhaTimeManager,
        healthConnectConfigDao: HealthConnectConfigDao,
        movementDao: MovementDao,
        constantsMapper: HealthConnectConstantsMapper,
        idManager: IdManager
    ): HealthConnectRepo {
        return HealthConnectRepoImpl(
            context = context,
            defaultScope = defaultScope,
            ioScope = ioScope,
            chunkManager = chunkManager,
            configRepo = configRepo,
            authRepo = authRepo,
            sensorRepo = sensorRepo,
            workManager = workManager,
            api = api,
            client = client,
            sahhaErrorLogger = sahhaErrorLogger,
            sahhaTimeManager = sahhaTimeManager,
            healthConnectConfigDao = healthConnectConfigDao,
            movementDao = movementDao,
            mapper = constantsMapper,
            idManager = idManager
        )
    }

    @Singleton
    @Provides
    fun provideActivityManager(context: Context): ActivityManager {
        return context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    @Singleton
    @Provides
    fun provideAppCrashRepo(
        activityManager: ActivityManager
    ): AppCrashRepo {
        return AppCrashRepoImpl(
            activityManager = activityManager
        )
    }

    @Singleton
    @Provides
    fun provideWorkManager(
        context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideNotificationManager(
        context: Context
    ): NotificationManager {
        return context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Singleton
    @Provides
    fun provideAlarmManager(
        context: Context
    ): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    @Singleton
    @Provides
    fun provideHealthConnectConstantsMapper(): HealthConnectConstantsMapper {
        return HealthConnectConstantsMapperImpl()
    }

    @Singleton
    @Provides
    fun provideCalculateBatchLimit(
        batchedDataRepo: BatchedDataRepo
    ): CalculateBatchLimit {
        return CalculateBatchLimit(
            batchedDataRepo
        )
    }

    @Singleton
    @Provides
    fun provideHostAppLifecycleObserver(
        context: Context,
        processor: AppEventProcessor,
        repository: BatchedDataRepo,
        permissionInteractionManager: PermissionInteractionManager,
        @IoScope ioScope: CoroutineScope
    ): HostAppLifecycleObserver {
        return HostAppLifecycleObserver(
            context,
            processor,
            repository,
            permissionInteractionManager,
            ioScope
          )
    }

    @Singleton
    @Provides
    fun providePermissionActionProvider(
        healthConnectRepo: HealthConnectRepo
    ): PermissionActionProvider {
        return PermissionActionProviderImpl(
            healthConnectRepo
        )
    }

    @Singleton
    @Provides
    fun provideHealthConnectMapperDefaults(
        mapper: HealthConnectConstantsMapper,
        timeManager: SahhaTimeManager,
        idManager: IdManager
    ): HealthConnectMapperDefaults {
        return HealthConnectMapperDefaults(
            mapper, timeManager, idManager
        )
    }

    @Singleton
    @Provides
    fun provideDatBatcherRunnable(
        context: Context,
        permissionManager: PermissionManager,
        sensorManager: SensorInteractionManager,
        configRepo: SahhaConfigRepo,
    ): DataBatcherRunnable {
        return DataBatcherRunnable(
            context,
            permissionManager,
            sensorManager,
            configRepo,
        )
    }

    @Singleton
    @Provides
    fun provideSensorToHealthConnectMetricMapper(): SensorToHealthConnectMetricMapper {
        return SensorToHealthConnectMetricMapper()
    }

    @Singleton
    @Provides
    fun provideBatchAggregateLogs(
        timeManager: SahhaTimeManager,
        provider: PermissionActionProvider,
        healthConnectRepo: HealthConnectRepo,
    ): BatchAggregateLogs {
        return BatchAggregateLogs(
            timeManager = timeManager,
            provider = provider,
            repository = healthConnectRepo
        )
    }

    @Singleton
    @Provides
    fun provideConnectionStateManager(
        context: Context
    ): ConnectionStateManager {
        return AndroidConnectionStateManager(context)
    }

    @Singleton
    @Provides
    fun provideAggregationDataTypeMapper(): AggregationDataTypeMapper {
        return AggregationDataTypeMapper()
    }

    @Singleton
    @Provides
    fun provideDataLogTransformer(mapper: AggregationDataTypeMapper): AggregateDataLogTransformer {
        return AggregateDataLogTransformer(mapper)
    }

    @Singleton
    @Provides
    fun provideAppEventProcessor(
        context: Context,
        mapper: HealthConnectConstantsMapper,
        manager: IdManager
    ): AppEventProcessor {
        return AppEventProcessorImpl(
            context, mapper, manager
        )
    }
}

