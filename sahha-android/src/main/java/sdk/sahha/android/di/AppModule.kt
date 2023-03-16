package sdk.sahha.android.di

import android.app.KeyguardManager
import android.app.Service
import android.content.Context
import android.hardware.SensorManager
import android.os.PowerManager
import androidx.health.connect.client.HealthConnectClient
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sdk.sahha.android.BuildConfig
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.data.local.SahhaDatabase
import sdk.sahha.android.data.local.SahhaDbMigrations
import sdk.sahha.android.data.local.dao.*
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.remote.SahhaErrorApi
import sdk.sahha.android.data.repository.*
import sdk.sahha.android.domain.repository.*
import sdk.sahha.android.source.SahhaEnvironment

internal object AppModule {
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
        encryptor: Encryptor,
        sahhaErrorLogger: SahhaErrorLogger
    ): AuthRepo {
        return AuthRepoImpl(encryptor, sahhaErrorLogger)
    }


    fun provideBackgroundRepository(
        context: Context,
        mainScope: CoroutineScope,
        configurationDao: ConfigurationDao,
        deviceDao: DeviceUsageDao,
        sleepDao: SleepDao,
        movementDao: MovementDao
    ): BackgroundRepo {
        return BackgroundRepoImpl(
            context,
            mainScope,
            configurationDao,
            deviceDao,
            sleepDao,
            movementDao
        )
    }


    fun providePermissionsRepository(
    ): PermissionsRepo {
        return PermissionsRepoImpl()
    }


    fun provideRemotePostRepository(
        sleepDao: SleepDao,
        deviceUsageDao: DeviceUsageDao,
        movementDao: MovementDao,
        encryptor: Encryptor,
        decryptor: Decryptor,
        api: SahhaApi,
        sahhaErrorLogger: SahhaErrorLogger,
        ioScope: CoroutineScope,
        timeManager: SahhaTimeManager
    ): RemoteRepo {
        return RemoteRepoImpl(
            sleepDao,
            deviceUsageDao,
            movementDao,
            encryptor,
            decryptor,
            api,
            sahhaErrorLogger,
            ioScope,
            timeManager
        )
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
                SahhaDbMigrations.MIGRATION_6_7
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
        decryptor: Decryptor,
        sahhaErrorApi: SahhaErrorApi,
        defaultScope: CoroutineScope
    ): SahhaErrorLogger {
        return SahhaErrorLogger(context, configurationDao, decryptor, sahhaErrorApi, defaultScope)
    }

    fun provideSensorManager(
        context: Context,
    ): SensorManager {
        return context.getSystemService(Service.SENSOR_SERVICE) as SensorManager
    }

    fun provideHealthConnectClient(
        context: Context,
    ): HealthConnectClient? {
        return if (HealthConnectClient.isAvailable(context)) HealthConnectClient.getOrCreate(context) else null
    }

    fun provideHealthConnectRepository(
        healthConnectClient: HealthConnectClient,
        timeManager: SahhaTimeManager,
        configurationDao: ConfigurationDao,
        sahhaApi: SahhaApi,
        sahhaErrorLogger: SahhaErrorLogger,
        decryptor: Decryptor
    ): HealthConnectRepo {
        return HealthConnectRepoImpl(
            healthConnectClient,
            timeManager,
            configurationDao,
            sahhaApi,
            sahhaErrorLogger,
            decryptor
        )
    }
}