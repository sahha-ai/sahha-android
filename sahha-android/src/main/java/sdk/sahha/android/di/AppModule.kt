package sdk.sahha.android.di

import android.content.Context
import androidx.activity.ComponentActivity
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
import sdk.sahha.android.common.AppCenterLog
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.data.local.SahhaDatabase
import sdk.sahha.android.data.local.dao.*
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.repository.AuthRepoImpl
import sdk.sahha.android.data.repository.BackgroundRepoImpl
import sdk.sahha.android.data.repository.PermissionsRepoImpl
import sdk.sahha.android.data.repository.RemoteRepoImpl
import sdk.sahha.android.domain.model.enums.SahhaEnvironment
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.BackgroundRepo
import sdk.sahha.android.domain.repository.PermissionsRepo
import sdk.sahha.android.domain.repository.RemoteRepo
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AppModule {
    init {
        System.loadLibrary("native-lib")
    }

    private external fun getApiUrlDev(): String
    private external fun getApiUrlProd(): String


    @Provides
    @Singleton
    fun provideSahhaApi(environment: Enum<SahhaEnvironment>): SahhaApi {
        return if (environment == SahhaEnvironment.development) {
            Retrofit.Builder()
                .baseUrl(getApiUrlDev())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SahhaApi::class.java)
        } else {
            Retrofit.Builder()
                .baseUrl(getApiUrlProd())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SahhaApi::class.java)
        }
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: SahhaApi,
        @Named("ioScope") ioScope: CoroutineScope,
        @Named("mainScope") mainScope: CoroutineScope,
        @ApplicationContext context: Context,
        encryptor: Encryptor
    ): AuthRepo {
        return AuthRepoImpl(context, api, ioScope, mainScope, encryptor)
    }

    @Provides
    @Singleton
    fun provideBackgroundRepository(
        @ApplicationContext context: Context,
        @Named("defaultScope") defaultScope: CoroutineScope,
        @Named("ioScope") ioScope: CoroutineScope,
        configurationDao: ConfigurationDao,
        api: SahhaApi
    ): BackgroundRepo {
        return BackgroundRepoImpl(
            context,
            defaultScope,
            ioScope,
            configurationDao,
            api
        )
    }

    @Provides
    @Singleton
    fun providePermissionsRepository(
        activity: ComponentActivity
    ): PermissionsRepo {
        return PermissionsRepoImpl(activity)
    }

    @Provides
    @Singleton
    fun provideRemotePostRepository(
        ioScope: CoroutineScope,
        sleepDao: SleepDao,
        deviceUsageDao: DeviceUsageDao,
        encryptor: Encryptor,
        decryptor: Decryptor,
        api: SahhaApi,
        appCenterLog: AppCenterLog
    ): RemoteRepo {
        return RemoteRepoImpl(
            ioScope,
            sleepDao,
            deviceUsageDao,
            encryptor,
            decryptor,
            api,
            appCenterLog
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
    fun provideAppCenterLog(
        @ApplicationContext context: Context,
        configurationDao: ConfigurationDao,
        @Named("defaultScope") defaultScope: CoroutineScope
    ): AppCenterLog {
        return AppCenterLog(context, configurationDao, defaultScope)
    }
}