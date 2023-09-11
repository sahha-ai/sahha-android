package sdk.sahha.android.di

import android.app.KeyguardManager
import android.app.Service
import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.os.PowerManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
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
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.local.SahhaDatabase
import sdk.sahha.android.data.local.SahhaDbUtility
import sdk.sahha.android.data.local.dao.*
import sdk.sahha.android.data.manager.PermissionManagerImpl
import sdk.sahha.android.data.manager.PostChunkManagerImpl
import sdk.sahha.android.data.manager.ReceiverManagerImpl
import sdk.sahha.android.data.manager.SahhaNotificationManagerImpl
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.remote.SahhaErrorApi
import sdk.sahha.android.data.repository.AuthRepoImpl
import sdk.sahha.android.data.repository.DeviceInfoRepoImpl
import sdk.sahha.android.data.repository.SahhaConfigRepoImpl
import sdk.sahha.android.data.repository.SensorRepoImpl
import sdk.sahha.android.data.repository.UserDataRepoImpl
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.manager.PostChunkManager
import sdk.sahha.android.domain.manager.ReceiverManager
import sdk.sahha.android.domain.manager.SahhaNotificationManager
import sdk.sahha.android.domain.model.categories.PermissionHandler
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.DeviceInfoRepo
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
        sahhaErrorLogger: SahhaErrorLogger
    ): SahhaNotificationManager {
        return SahhaNotificationManagerImpl(context, sahhaErrorLogger)
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
        environment: Enum<SahhaEnvironment>,
        gson: GsonConverterFactory,
        okHttpClient: OkHttpClient
    ): SahhaApi {
        return if (environment == SahhaEnvironment.production) {
            Retrofit.Builder()
                .baseUrl(BuildConfig.API_PROD)
                .client(okHttpClient)
                .addConverterFactory(gson)
                .build()
                .create(SahhaApi::class.java)
        } else {
            Retrofit.Builder()
                .baseUrl(BuildConfig.API_DEV)
                .client(okHttpClient)
                .addConverterFactory(gson)
                .build()
                .create(SahhaApi::class.java)
        }
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

    @Singleton
    @Provides
    fun provideSahhaErrorApi(
        environment: Enum<SahhaEnvironment>,
        gson: GsonConverterFactory
    ): SahhaErrorApi {
        return if (environment == SahhaEnvironment.production) {
            Retrofit.Builder()
                .baseUrl(BuildConfig.API_PROD)
                .addConverterFactory(gson)
                .build()
                .create(SahhaErrorApi::class.java)
        } else {
            Retrofit.Builder()
                .baseUrl(BuildConfig.API_DEV)
                .addConverterFactory(gson)
                .build()
                .create(SahhaErrorApi::class.java)
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
        chunkManager: PostChunkManager
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
            chunkManager
        )
    }

    @Singleton
    @Provides
    fun providePermissionHandler(): PermissionHandler {
        return PermissionHandler()
    }

    @Singleton
    @Provides
    fun providePermissionManager(
        permissionHandler: PermissionHandler,
        sahhaErrorLogger: SahhaErrorLogger
    ): PermissionManager {
        return PermissionManagerImpl(permissionHandler, sahhaErrorLogger)
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
}