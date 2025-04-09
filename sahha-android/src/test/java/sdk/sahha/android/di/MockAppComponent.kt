package sdk.sahha.android.di

import android.app.KeyguardManager
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.os.PowerManager
import androidx.health.connect.client.HealthConnectClient
import io.mockk.mockk
import kotlinx.coroutines.sync.Mutex
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.mock.NetworkBehavior
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
import sdk.sahha.android.data.repository.MockAuthRepoImpl
import sdk.sahha.android.data.repository.MockBatchedDataRepoImpl
import sdk.sahha.android.data.repository.MockSahhaConfigRepo
import sdk.sahha.android.data.repository.UserDataRepoImpl
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

internal class MockAppComponent : AppComponent {
    private val behavior = NetworkBehavior.create()

    override val mainScope = AppModule.mainScope

    override val defaultScope = AppModule.defaultScope
    override val timeManager: SahhaTimeManager
        get() = AppModule.mockSahhaTimeManager
    override val encryptor: Encryptor
        get() = TODO("Not yet implemented")
    override val decryptor: Decryptor
        get() = TODO("Not yet implemented")
    override val healthConnectClient: HealthConnectClient?
        get() = AppModule.mockHealthConnectClient
    override val healthConnectRepo: HealthConnectRepo
        get() = AppModule.mockHealthConnectRepo
    override val healthConnectConstantsMapper: HealthConnectConstantsMapper
        get() = AppModule.mockHealthConnectConstantsMapper
    override val postHealthConnectDataUseCase: PostHealthConnectDataUseCase
        get() = TODO("Not yet implemented")
    override val logAppAliveState: LogAppAliveState
        get() = TODO("Not yet implemented")
    override val batchAggregateLogs: BatchAggregateLogs
        get() = AppModule.mockBatchAggregateLogs
    override val connectionStateManager: ConnectionStateManager
        get() = AppModule.mockConnectionStateManager
    override val dataLogTransformer: AggregateDataLogTransformer
        get() = AppModule.mockDataLogTransformer
    override val ioScope = AppModule.ioScope
    override val sahhaEnvironment: Enum<SahhaEnvironment>
        get() = TODO("Not yet implemented")
    override val database: SahhaDatabase
        get() = TODO("Not yet implemented")
    override val sahhaInteractionManager: SahhaInteractionManager
        get() = TODO()
    override val mutex: Mutex
        get() = AppModule.mutex
    override val sensorRepo: SensorRepo
        get() = AppModule.mockSensorRepo
    override val powerManager: PowerManager
        get() = TODO("Not yet implemented")
    override val keyguardManager: KeyguardManager
        get() = TODO("Not yet implemented")
    override val sensorManager: SensorManager
        get() = TODO("Not yet implemented")
    override val sahhaNotificationManager: SahhaNotificationManager
        get() = TODO("Not yet implemented")
    override val receiverManager: ReceiverManager
        get() = TODO("Not yet implemented")
    override val permissionHandler: PermissionHandler
        get() = TODO("Not yet implemented")
    override val permissionManager: PermissionManager
        get() = TODO("Not yet implemented")
    override val postChunkManager: PostChunkManager
        get() = AppModule.postChunkManager
    override val encryptedSharedPreferences: SharedPreferences
        get() = TODO("Not yet implemented")
    override val sahhaErrorLogger: SahhaErrorLogger
        get() = SahhaErrorLogger(
            mockk(),
            AppModule.mockSahhaErrorApi,
            mainScope,
            MockAuthRepoImpl(),
            MockSahhaConfigRepo()
        )
    override val hostAppLifecycleObserver: HostAppLifecycleObserver
        get() = TODO("Not yet implemented")
    override val dataBatcherRunnable: DataBatcherRunnable
        get() = TODO("Not yet implemented")
    override val gson: GsonConverterFactory
        get() = TODO("Not yet implemented")
    override val api: SahhaApi
        get() = AppModule.mockSahhaApi
    override val sahhaErrorApi: SahhaErrorApi
        get() = AppModule.mockSahhaErrorApi
    override val securityDao: SecurityDao
        get() = TODO("Not yet implemented")
    override val movementDao: MovementDao
        get() = TODO("Not yet implemented")
    override val sleepDao: SleepDao
        get() = TODO("Not yet implemented")
    override val configurationDao: ConfigurationDao
        get() = TODO("Not yet implemented")
    override val deviceUsageRepo: DeviceUsageRepo
        get() = TODO("Not yet implemented")
    override val authRepo: AuthRepo
        get() = TODO("Not yet implemented")
    override val deviceInfoRepo: DeviceInfoRepo
        get() = TODO("Not yet implemented")
    override var userDataRepo = UserDataRepoImpl(
        ioScope,
        AppModule.mockAuthRepo,
        AppModule.mockSahhaApi,
        AppModule.mockSahhaErrorLogger
    )
    override val sahhaConfigRepo: SahhaConfigRepo
        get() = AppModule.mockSahhaConfigRepo
    override val insightsRepo: InsightsRepo
        get() = TODO("Not yet implemented")
    override val batchedDataRepo: BatchedDataRepo
        get() = AppModule.mockBatchedDataRepo
    override val batchDataLogs: BatchDataLogs
        get() = TODO("Not yet implemented")
    override val postBatchData: PostBatchData
        get() = TODO("Not yet implemented")
    override val getStatsUseCase: GetStatsUseCase
        get() = TODO("Not yet implemented")
    override val getSamplesUseCase: GetSamplesUseCase
        get() = TODO("Not yet implemented")
    override val getBiomarkersUseCase: GetBiomarkersUseCase
        get() = TODO("Not yet implemented")
    override val mapperDefaults = HealthConnectMapperDefaults(
        AppModule.mockHealthConnectConstantsMapper,
        AppModule.mockSahhaTimeManager,
        AppModule.mockIdManager
    )
}
