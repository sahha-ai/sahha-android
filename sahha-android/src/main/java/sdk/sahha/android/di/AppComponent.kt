package sdk.sahha.android.di

import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.os.PowerManager
import androidx.health.connect.client.HealthConnectClient
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
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.data.local.dao.SecurityDao
import sdk.sahha.android.data.local.dao.SleepDao
import sdk.sahha.android.data.mapper.HealthConnectMapperDefaults
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.remote.SahhaErrorApi
import sdk.sahha.android.domain.interaction.SahhaInteractionManager
import sdk.sahha.android.domain.manager.ConnectionStateManager
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.manager.PostChunkManager
import sdk.sahha.android.domain.manager.ReceiverManager
import sdk.sahha.android.domain.manager.SahhaNotificationManager
import sdk.sahha.android.domain.mapper.HealthConnectConstantsMapper
import sdk.sahha.android.domain.model.categories.PermissionHandler
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.BatchedDataRepo
import sdk.sahha.android.domain.repository.DeviceInfoRepo
import sdk.sahha.android.domain.repository.DeviceUsageRepo
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.domain.repository.InsightsRepo
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.repository.UserDataRepo
import sdk.sahha.android.domain.transformer.AggregateDataLogTransformer
import sdk.sahha.android.domain.use_case.GetBiomarkersUseCase
import sdk.sahha.android.domain.use_case.GetSamplesUseCase
import sdk.sahha.android.domain.use_case.GetStatsUseCase
import sdk.sahha.android.domain.use_case.background.BatchAggregateLogs
import sdk.sahha.android.domain.use_case.background.BatchDataLogs
import sdk.sahha.android.domain.use_case.background.LogAppAliveState
import sdk.sahha.android.domain.use_case.post.PostBatchData
import sdk.sahha.android.domain.use_case.post.PostHealthConnectDataUseCase
import sdk.sahha.android.framework.observer.HostAppLifecycleObserver
import sdk.sahha.android.framework.runnable.DataBatcherRunnable
import sdk.sahha.android.source.SahhaEnvironment
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
    val sahhaNotificationManager: SahhaNotificationManager
    val receiverManager: ReceiverManager
    val permissionHandler: PermissionHandler
    val permissionManager: PermissionManager
    val postChunkManager: PostChunkManager
    val encryptedSharedPreferences: SharedPreferences
    val sahhaErrorLogger: SahhaErrorLogger
    val hostAppLifecycleObserver: HostAppLifecycleObserver
    val dataBatcherRunnable: DataBatcherRunnable

    val gson: GsonConverterFactory
    val api: SahhaApi
    val sahhaErrorApi: SahhaErrorApi
    val securityDao: SecurityDao
    val movementDao: MovementDao
    val sleepDao: SleepDao
    val configurationDao: ConfigurationDao

    val deviceUsageRepo: DeviceUsageRepo
    val authRepo: AuthRepo
    val deviceInfoRepo: DeviceInfoRepo
    val userDataRepo: UserDataRepo
    val sahhaConfigRepo: SahhaConfigRepo
    val insightsRepo: InsightsRepo
    val batchedDataRepo: BatchedDataRepo

    val batchDataLogs: BatchDataLogs
    val postBatchData: PostBatchData
    val getStatsUseCase: GetStatsUseCase
    val getSamplesUseCase: GetSamplesUseCase
    val getBiomarkersUseCase: GetBiomarkersUseCase

    val mapperDefaults: HealthConnectMapperDefaults

    @get:MainScope
    val mainScope: CoroutineScope

    @get:IoScope
    val ioScope: CoroutineScope

    @get:DefaultScope
    val defaultScope: CoroutineScope

    val timeManager: SahhaTimeManager
    val encryptor: Encryptor
    val decryptor: Decryptor
    val healthConnectClient: HealthConnectClient?
    val healthConnectRepo: HealthConnectRepo
    val healthConnectConstantsMapper: HealthConnectConstantsMapper

    val postHealthConnectDataUseCase: PostHealthConnectDataUseCase
    val logAppAliveState: LogAppAliveState
    val batchAggregateLogs: BatchAggregateLogs

    val connectionStateManager: ConnectionStateManager
    val dataLogTransformer: AggregateDataLogTransformer
}