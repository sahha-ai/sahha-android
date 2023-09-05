package sdk.sahha.android.di

import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.os.PowerManager
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import retrofit2.converter.gson.GsonConverterFactory
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.data.local.SahhaDatabase
import sdk.sahha.android.data.local.dao.*
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.remote.SahhaErrorApi
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
import sdk.sahha.android.interaction.SahhaInteractionManager
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSensor
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
internal interface AppComponent {
    @Component.Builder
    interface Builder {
        fun build(): AppComponent
        fun appModule(appModule: AppModule): Builder

        @BindsInstance
        fun context(context: Context): Builder
    }

    val sahhaEnvironment: Enum<SahhaEnvironment>
    val database: SahhaDatabase
    val sahhaInteractionManager: SahhaInteractionManager
    val mutex: Mutex
    val sensorRepo: SensorRepo
    val powerManager: PowerManager
    val keyguardManager: KeyguardManager
    val sensorManager: SensorManager
    val notificationManager: SahhaNotificationManager
    val receiverManager: ReceiverManager
    val permissionHandler: PermissionHandler
    val permissionManager: PermissionManager
    val postChunkManager: PostChunkManager
    val encryptedSharedPreferences: SharedPreferences
    val sahhaErrorLogger: SahhaErrorLogger

    val gson: GsonConverterFactory
    val api: SahhaApi
    val sahhaErrorApi: SahhaErrorApi
    val securityDao: SecurityDao
    val movementDao: MovementDao
    val sleepDao: SleepDao
    val deviceUsageDao: DeviceUsageDao
    val configurationDao: ConfigurationDao

    val authRepo: AuthRepo
    val deviceInfoRepo: DeviceInfoRepo
    val userDataRepo: UserDataRepo
    val sahhaConfigRepo: SahhaConfigRepo

    @get:MainScope
    val mainScope: CoroutineScope

    @get:IoScope
    val ioScope: CoroutineScope

    @get:DefaultScope
    val defaultScope: CoroutineScope

    val timeManager: SahhaTimeManager
    val encryptor: Encryptor
    val decryptor: Decryptor
}