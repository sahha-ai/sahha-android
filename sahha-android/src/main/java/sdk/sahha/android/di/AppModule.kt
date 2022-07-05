package sdk.sahha.android.di

import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sdk.sahha.android.BuildConfig
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.data.local.SahhaDatabase
import sdk.sahha.android.data.local.SahhaDbMigrations
import sdk.sahha.android.data.local.dao.*
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.remote.SahhaErrorApi
import sdk.sahha.android.data.repository.AuthRepoImpl
import sdk.sahha.android.data.repository.BackgroundRepoImpl
import sdk.sahha.android.data.repository.PermissionsRepoImpl
import sdk.sahha.android.data.repository.RemoteRepoImpl
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.BackgroundRepo
import sdk.sahha.android.domain.repository.PermissionsRepo
import sdk.sahha.android.domain.repository.RemoteRepo
import sdk.sahha.android.source.SahhaEnvironment
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AppModule {
    @Provides
    @Singleton
    fun providePowerManager(
        @ApplicationContext context: Context
    ): PowerManager {
        return context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    @Provides
    @Singleton
    fun provideKeyguardManager(
        @ApplicationContext context: Context
    ): KeyguardManager {
        return context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    }

    @Provides
    @Singleton
    fun provideGsonConverter(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    @Provides
    @Singleton
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

    @Provides
    @Singleton
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


    @Provides
    @Singleton
    fun provideAuthRepository(
        encryptor: Encryptor,
        sahhaErrorLogger: SahhaErrorLogger
    ): AuthRepo {
        return AuthRepoImpl(encryptor, sahhaErrorLogger)
    }

    @Provides
    @Singleton
    fun provideBackgroundRepository(
        @ApplicationContext context: Context,
        @Named("defaultScope") defaultScope: CoroutineScope,
        @Named("ioScope") ioScope: CoroutineScope,
        configurationDao: ConfigurationDao,
    ): BackgroundRepo {
        return BackgroundRepoImpl(
            context,
            defaultScope,
            ioScope,
            configurationDao,
        )
    }

    @Provides
    @Singleton
    fun providePermissionsRepository(
    ): PermissionsRepo {
        return PermissionsRepoImpl()
    }

    @Provides
    @Singleton
    fun provideRemotePostRepository(
        sleepDao: SleepDao,
        deviceUsageDao: DeviceUsageDao,
        movementDao: MovementDao,
        encryptor: Encryptor,
        decryptor: Decryptor,
        api: SahhaApi,
        sahhaErrorLogger: SahhaErrorLogger,
        @Named("ioScope") ioScope: CoroutineScope
    ): RemoteRepo {
        return RemoteRepoImpl(
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

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SahhaDatabase {
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
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideMovementDao(db: SahhaDatabase): MovementDao {
        return db.movementDao()
    }

    @Provides
    @Singleton
    fun provideSecurityDao(db: SahhaDatabase): SecurityDao {
        return db.securityDao()
    }

    @Provides
    @Singleton
    fun provideSleepDao(db: SahhaDatabase): SleepDao {
        return db.sleepDao()
    }

    @Provides
    @Singleton
    fun provideDeviceUsageDao(db: SahhaDatabase): DeviceUsageDao {
        return db.deviceUsageDao()
    }

    @Provides
    @Singleton
    fun provideConfigDao(db: SahhaDatabase): ConfigurationDao {
        return db.configurationDao()
    }

    @Provides
    @Singleton
    @Named("defaultScope")
    fun provideDefaultScope(): CoroutineScope {
        return CoroutineScope(Default)
    }

    @Provides
    @Singleton
    @Named("ioScope")
    fun provideIoScope(): CoroutineScope {
        return CoroutineScope(IO)
    }

    @Provides
    @Singleton
    @Named("mainScope")
    fun provideMainScope(): CoroutineScope {
        return CoroutineScope(Main)
    }

    @Provides
    @Singleton
    fun provideSahhaErrorLogger(
        @ApplicationContext context: Context,
        configurationDao: ConfigurationDao,
        decryptor: Decryptor,
        sahhaErrorApi: SahhaErrorApi,
        @Named("defaultScope") defaultScope: CoroutineScope
    ): SahhaErrorLogger {
        return SahhaErrorLogger(context, configurationDao, decryptor, sahhaErrorApi, defaultScope)
    }
}