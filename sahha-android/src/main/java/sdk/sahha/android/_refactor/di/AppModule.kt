package sdk.sahha.android._refactor.di

import android.content.Context
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
import sdk.sahha.android._refactor.data.local.SahhaDatabase
import sdk.sahha.android._refactor.data.local.dao.MovementDao
import sdk.sahha.android._refactor.data.local.dao.SecurityDao
import sdk.sahha.android._refactor.data.remote.SahhaApi
import sdk.sahha.android._refactor.data.repository.AuthRepoImpl
import sdk.sahha.android._refactor.data.repository.BackgroundRepoImpl
import sdk.sahha.android._refactor.domain.repository.AuthRepo
import sdk.sahha.android._refactor.domain.repository.BackgroundRepo
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AppModule {

    @Provides
    @Singleton
    fun provideSahhaApi(): SahhaApi {
        return Retrofit.Builder()
            .baseUrl("")
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