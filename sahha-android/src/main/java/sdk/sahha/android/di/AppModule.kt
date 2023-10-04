package sdk.sahha.android.di

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.os.PowerManager
import androidx.health.connect.client.HealthConnectClient
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
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
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.Session
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.local.SahhaDatabase
import sdk.sahha.android.data.local.SahhaDbUtility
import sdk.sahha.android.data.local.dao.*
import sdk.sahha.android.data.manager.PermissionManagerImpl
import sdk.sahha.android.data.manager.PostChunkManagerImpl
import sdk.sahha.android.data.manager.ReceiverManagerImpl
import sdk.sahha.android.data.manager.SahhaAlarmManagerImpl
import sdk.sahha.android.data.manager.SahhaNotificationManagerImpl
import sdk.sahha.android.data.mapper.HealthConnectConstantsMapperImpl
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.remote.SahhaErrorApi
import sdk.sahha.android.data.repository.AuthRepoImpl
import sdk.sahha.android.data.repository.DeviceInfoRepoImpl
import sdk.sahha.android.data.repository.HealthConnectRepoImpl
import sdk.sahha.android.data.repository.SahhaConfigRepoImpl
import sdk.sahha.android.data.repository.SensorRepoImpl
import sdk.sahha.android.data.repository.UserDataRepoImpl
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.manager.PostChunkManager
import sdk.sahha.android.domain.manager.ReceiverManager
import sdk.sahha.android.domain.manager.SahhaAlarmManager
import sdk.sahha.android.domain.manager.SahhaNotificationManager
import sdk.sahha.android.domain.mapper.HealthConnectConstantsMapper
import sdk.sahha.android.domain.model.callbacks.ActivityCallback
import sdk.sahha.android.domain.model.categories.PermissionHandler
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.DeviceInfoRepo
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.repository.UserDataRepo
import sdk.sahha.android.domain.use_case.background.*
import sdk.sahha.android.domain.use_case.post.*
import sdk.sahha.android.source.SahhaEnvironment
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

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
    fun provideDeviceInfoRepo(
        authRepo: AuthRepo,
        api: SahhaApi,
        sahhaErrorLogger: SahhaErrorLogger
    ): DeviceInfoRepo {
        return DeviceInfoRepoImpl(
            authRepo,
            api,
            sahhaErrorLogger
        )
    }

    @Singleton
    @Provides
    fun provideSahhaNotificationManager(
        context: Context,
        sahhaErrorLogger: SahhaErrorLogger,
        notificationManager: NotificationManager
    ): SahhaNotificationManager {
        return SahhaNotificationManagerImpl(context, sahhaErrorLogger, notificationManager)
    }

    @Singleton
    @Provides
    fun provideEncryptedSharedPreferences(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        return EncryptedSharedPreferences.create(
            "encrypted_prefs",
            masterKeyAlias,
            context,
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
        val shouldBeDevEnv =
            Session.shouldBeDevEnvironment(
                context, environment
            )

        return if (shouldBeDevEnv) {
            Retrofit.Builder()
                .baseUrl(BuildConfig.API_DEV)
                .client(okHttpClient)
                .addConverterFactory(gson)
                .build()
                .create(apiClass)
        } else if (environment == SahhaEnvironment.production) {
            Retrofit.Builder()
                .baseUrl(BuildConfig.API_PROD)
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
        deviceDao: DeviceUsageDao,
        sleepDao: SleepDao,
        movementDao: MovementDao,
        authRepo: AuthRepo,
        sahhaErrorLogger: SahhaErrorLogger,
        mutex: Mutex,
        api: SahhaApi,
        chunkManager: PostChunkManager,
        permissionManager: PermissionManager
    ): SensorRepo {
        return SensorRepoImpl(
            context,
            defaultScope,
            ioScope,
            deviceDao,
            sleepDao,
            movementDao,
            authRepo,
            sahhaConfigRepo,
            sahhaErrorLogger,
            mutex,
            api,
            chunkManager,
            permissionManager
        )
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
        healthConnectClient: HealthConnectClient?,
        sahhaErrorLogger: SahhaErrorLogger,
        @DefaultScope defaultScope: CoroutineScope,
    ): PermissionManager {
        return PermissionManagerImpl(
            defaultScope,
            permissionHandler,
            healthConnectClient,
            sahhaErrorLogger
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
    fun provideTestDataDao(db: SahhaDatabase): TestDataDao {
        return db.testDataDao()
    }

    @DefaultScope
    @Singleton
    @Provides
    fun provideDefaultScope(): CoroutineScope {
        return CoroutineScope(Default)
    }

    @IoScope
    @Singleton
    @Provides
    fun provideIoScope(): CoroutineScope {
        return CoroutineScope(IO)
    }

    @MainScope
    @Singleton
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

    @Singleton
    @Provides
    fun providePostChunkManager(): PostChunkManager {
        return PostChunkManagerImpl()
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
        permissionManager: PermissionManager,
        configRepo: SahhaConfigRepo,
        authRepo: AuthRepo,
        sensorRepo: SensorRepo,
        workManager: WorkManager,
        api: SahhaApi,
        client: HealthConnectClient?,
        sahhaErrorLogger: SahhaErrorLogger,
        sahhaTimeManager: SahhaTimeManager,
        healthConnectConfigDao: HealthConnectConfigDao,
        sahhaAlarmManager: SahhaAlarmManager,
        movementDao: MovementDao,
    ): HealthConnectRepo {
        return HealthConnectRepoImpl(
            context,
            defaultScope,
            ioScope,
            chunkManager,
            permissionManager,
            configRepo,
            authRepo,
            sensorRepo,
            workManager,
            api,
            client,
            sahhaErrorLogger,
            sahhaTimeManager,
            healthConnectConfigDao,
            sahhaAlarmManager,
            movementDao,
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
    fun provideSahhaAlarmManager(
        alarmManager: AlarmManager
    ): SahhaAlarmManager {
        return SahhaAlarmManagerImpl(
            alarmManager
        )
    }

    @Singleton
    @Provides
    fun provideHealthConnectConstantsMapper(): HealthConnectConstantsMapper {
        return HealthConnectConstantsMapperImpl()
    }
}