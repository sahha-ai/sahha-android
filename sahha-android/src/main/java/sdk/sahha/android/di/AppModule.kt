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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.data.Constants.BASE_URL
import sdk.sahha.android.data.local.SahhaDatabase
import sdk.sahha.android.data.local.dao.*
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.repository.AuthRepoImpl
import sdk.sahha.android.data.repository.BackgroundRepoImpl
import sdk.sahha.android.data.repository.PermissionsRepoImpl
import sdk.sahha.android.data.repository.RemotePostRepoImpl
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.BackgroundRepo
import sdk.sahha.android.domain.repository.PermissionsRepo
import sdk.sahha.android.domain.repository.RemotePostRepo
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AppModule {

    @Provides
    @Singleton
    fun provideSahhaApi(): SahhaApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SahhaApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: SahhaApi,
        ioScope: CoroutineScope,
        @ApplicationContext context: Context,
        securityDao: SecurityDao
    ): AuthRepo {
        return AuthRepoImpl(context, api, ioScope, securityDao)
    }

    @Provides
    @Singleton
    fun provideBackgroundRepository(
        @ApplicationContext context: Context,
        defaultScope: CoroutineScope
    ): BackgroundRepo {
        return BackgroundRepoImpl(context, defaultScope)
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
    fun provideSleepWorkerRepository(
        ioScope: CoroutineScope,
        sleepDao: SleepDao,
        securityDao: SecurityDao,
        timeManager: SahhaTimeManager,
        decryptor: Decryptor,
        api: SahhaApi
    ): RemotePostRepo {
        return RemotePostRepoImpl(
            ioScope,
            sleepDao,
            securityDao,
            timeManager,
            decryptor,
            api
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
}