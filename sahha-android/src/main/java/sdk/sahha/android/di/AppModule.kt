package sdk.sahha.android.di

import android.app.KeyguardManager
import android.app.Service
import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.os.PowerManager
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.sync.Mutex
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sdk.sahha.android.BuildConfig
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.data.local.SahhaDatabase
import sdk.sahha.android.data.local.SahhaDbMigrations
import sdk.sahha.android.data.local.dao.*
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.remote.SahhaErrorApi
import sdk.sahha.android.data.repository.AuthRepoImpl
import sdk.sahha.android.data.repository.SensorRepoImpl
import sdk.sahha.android.data.manager.PermissionManagerImpl
import sdk.sahha.android.data.manager.ReceiverManagerImpl
import sdk.sahha.android.data.manager.SahhaNotificationManagerImpl
import sdk.sahha.android.data.repository.DeviceInfoRepoImpl
import sdk.sahha.android.data.repository.UserDataRepoImpl
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.manager.ReceiverManager
import sdk.sahha.android.domain.manager.SahhaNotificationManager
import sdk.sahha.android.domain.repository.DeviceInfoRepo
import sdk.sahha.android.domain.repository.UserDataRepo
import sdk.sahha.android.source.SahhaEnvironment

internal object AppModule {
    fun provideUserDataRepo(
        mainScope: CoroutineScope,
        authRepo: AuthRepo,
        api: SahhaApi,
        sahhaErrorLogger: SahhaErrorLogger
    ): UserDataRepo {
        return UserDataRepoImpl(
            mainScope,
            authRepo,
            api,
            sahhaErrorLogger
        )
    }
    fun provideReceiverManager(
        context: Context,
        mainScope: CoroutineScope
    ): ReceiverManager {
        return ReceiverManagerImpl(
            context,
            mainScope
        )
    }
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
    fun provideSahhaNotificationManager(context: Context): SahhaNotificationManager {
        return SahhaNotificationManagerImpl(context)
    }

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

    fun providePowerManager(
        context: Context
    ): PowerManager {
        return context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    fun provideKeyguardManager(
        context: Context
    ): KeyguardManager {
        return context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    }


    fun provideGsonConverter(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }


    fun provideSahhaApi(
        environment: Enum<SahhaEnvironment>,
        gson: GsonConverterFactory
    ): SahhaApi {
        return if (environment == SahhaEnvironment.production) {
            Retrofit.Builder()
                .baseUrl(BuildConfig.API_PROD)
                .addConverterFactory(gson)
                .build()
                .create(SahhaApi::class.java)
        } else {
            Retrofit.Builder()
                .baseUrl(BuildConfig.API_DEV)
                .addConverterFactory(gson)
                .build()
                .create(SahhaApi::class.java)
        }
    }


    fun provideSahhaErrorApi(
        environment: Enum<SahhaEnvironment>,
        gson: GsonConverterFactory
    ): SahhaErrorApi {
        return if (environment == SahhaEnvironment.production) {
            Retrofit.Builder()
                .baseUrl(BuildConfig.ERROR_API_PROD)
                .addConverterFactory(gson)
                .build()
                .create(SahhaErrorApi::class.java)
        } else {
            Retrofit.Builder()
                .baseUrl(BuildConfig.ERROR_API_DEV)
                .addConverterFactory(gson)
                .build()
                .create(SahhaErrorApi::class.java)
        }
    }


    fun provideAuthRepository(
        api: SahhaApi,
        encryptedSharedPreferences: SharedPreferences,
    ): AuthRepo {
        return AuthRepoImpl(
            api,
            encryptedSharedPreferences,
        )
    }


    fun provideSensorRepository(
        context: Context,
        mainScope: CoroutineScope,
        configurationDao: ConfigurationDao,
        deviceDao: DeviceUsageDao,
        sleepDao: SleepDao,
        movementDao: MovementDao,
        authRepo: AuthRepo,
        sahhaErrorLogger: SahhaErrorLogger,
        mutex: Mutex,
        api: SahhaApi
    ): SensorRepo {
        return SensorRepoImpl(
            context,
            mainScope,
            configurationDao,
            deviceDao,
            sleepDao,
            movementDao,
            authRepo,
            sahhaErrorLogger,
            mutex,
            api
        )
    }


    fun providePermissionsRepository(
    ): PermissionManager {
        return PermissionManagerImpl()
    }


    fun provideDatabase(context: Context): SahhaDatabase {
        return Room.databaseBuilder(
            context,
            SahhaDatabase::class.java,
            "sahha-database"
        )
            .fallbackToDestructiveMigration()
            .addMigrations(
                SahhaDbMigrations.MIGRATION_1_2,
                SahhaDbMigrations.MIGRATION_2_3,
                SahhaDbMigrations.MIGRATION_3_4,
                SahhaDbMigrations.MIGRATION_4_5,
                SahhaDbMigrations.MIGRATION_5_6,
            )
            .build()
    }


    fun provideMovementDao(db: SahhaDatabase): MovementDao {
        return db.movementDao()
    }


    fun provideSecurityDao(db: SahhaDatabase): SecurityDao {
        return db.securityDao()
    }


    fun provideSleepDao(db: SahhaDatabase): SleepDao {
        return db.sleepDao()
    }


    fun provideDeviceUsageDao(db: SahhaDatabase): DeviceUsageDao {
        return db.deviceUsageDao()
    }


    fun provideConfigDao(db: SahhaDatabase): ConfigurationDao {
        return db.configurationDao()
    }


    fun provideDefaultScope(): CoroutineScope {
        return CoroutineScope(Default)
    }


    fun provideIoScope(): CoroutineScope {
        return CoroutineScope(IO)
    }


    fun provideMainScope(): CoroutineScope {
        return CoroutineScope(Main)
    }


    fun provideSahhaErrorLogger(
        context: Context,
        configurationDao: ConfigurationDao,
        sahhaErrorApi: SahhaErrorApi,
        defaultScope: CoroutineScope,
        authRepo: AuthRepo
    ): SahhaErrorLogger {
        return SahhaErrorLogger(
            context,
            configurationDao,
            sahhaErrorApi,
            defaultScope,
            authRepo
        )
    }


    fun provideSensorManager(
        context: Context,
    ): SensorManager {
        return context.getSystemService(Service.SENSOR_SERVICE) as SensorManager
    }
}