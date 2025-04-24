package sdk.sahha.android.di

import androidx.health.connect.client.testing.FakeHealthConnectClient
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.sync.Mutex
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.data.manager.MockIdManager
import sdk.sahha.android.data.manager.PostChunkManagerImpl
import sdk.sahha.android.data.provider.PermissionActionProviderImpl
import sdk.sahha.android.data.remote.MockSahhaApi
import sdk.sahha.android.data.remote.MockSahhaErrorApi
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.remote.SahhaErrorApi
import sdk.sahha.android.data.repository.MockAuthRepoImpl
import sdk.sahha.android.data.repository.MockBatchedDataRepoImpl
import sdk.sahha.android.data.repository.MockDeviceInfoRepoImpl
import sdk.sahha.android.data.repository.MockDeviceUsageRepoImpl
import sdk.sahha.android.data.repository.MockHealthConnectRepoImpl
import sdk.sahha.android.data.repository.MockSahhaConfigRepo
import sdk.sahha.android.data.repository.MockSensorRepoImpl
import sdk.sahha.android.data.repository.MockSleepRepoImpl
import sdk.sahha.android.data.repository.MockUserDataRepoImpl
import sdk.sahha.android.data.repository.UserDataRepoImpl
import sdk.sahha.android.domain.interaction.UserDataInteractionManager
import sdk.sahha.android.domain.mapper.AggregationDataTypeMapper
import sdk.sahha.android.domain.transformer.AggregateDataLogTransformer
import sdk.sahha.android.domain.use_case.CalculateBatchLimit
import sdk.sahha.android.domain.use_case.GetDemographicUseCase
import sdk.sahha.android.domain.use_case.GetScoresUseCase
import sdk.sahha.android.domain.use_case.SaveTokensUseCase
import sdk.sahha.android.domain.use_case.background.BatchAggregateLogs
import sdk.sahha.android.domain.use_case.background.FilterActivityOverlaps
import sdk.sahha.android.domain.use_case.metadata.AddMetadata
import sdk.sahha.android.domain.use_case.post.PostDemographicUseCase
import sdk.sahha.android.framework.MockHealthConnectConstantsMapperImpl
import sdk.sahha.android.framework.manager.AndroidConnectionStateManager
import sdk.sahha.android.framework.mapper.SensorToHealthConnectMetricMapper

internal object AppModule {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.placeholder.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val behavior = NetworkBehavior.create()

    val mockRetrofit = MockRetrofit.Builder(retrofit)
        .networkBehavior(behavior)
        .build()

    val sahhaApi = retrofit.create(SahhaApi::class.java)

    val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    val defaultScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val delegateApi = mockRetrofit.create(SahhaApi::class.java)
    val delegateErrorApi = mockRetrofit.create(SahhaErrorApi::class.java)
    val mockSahhaApi = MockSahhaApi(delegateApi)
    val mockSahhaErrorApi = MockSahhaErrorApi(delegateErrorApi)
    val mockSahhaTimeManager = SahhaTimeManager()
    val mockBatchedDataRepo = MockBatchedDataRepoImpl()
    val mockHealthConnectRepo = MockHealthConnectRepoImpl()
    val mockAuthRepo = MockAuthRepoImpl()
    val mockDeviceInfoRepo = MockDeviceInfoRepoImpl()
    val mockSahhaConfigRepo = MockSahhaConfigRepo()
    val mockUserDataRepo = MockUserDataRepoImpl()
    val mockDeviceUsageRepo = MockDeviceUsageRepoImpl()
    val mockSleepRepo = MockSleepRepoImpl()
    val mockSensorRepo = MockSensorRepoImpl()
    val mockHealthConnectConstantsMapper = MockHealthConnectConstantsMapperImpl()
    val mockSahhaErrorLogger = SahhaErrorLogger(
        mockk(),
        mockSahhaErrorApi,
        mainScope,
        mockAuthRepo,
        mockSahhaConfigRepo
    )
    val mockIdManager = MockIdManager()
    val filterOverlaps = FilterActivityOverlaps(
        mockBatchedDataRepo, mockSahhaTimeManager
    )

    val getScores = GetScoresUseCase(
        repository = mockUserDataRepo,
        sahhaTimeManager = mockSahhaTimeManager,
        sahhaErrorLogger = null,
    )
    val postDemographic = PostDemographicUseCase(
        repository = mockUserDataRepo
    )
    val addMetadata = AddMetadata(
        timeManager = mockSahhaTimeManager
    )
    var userDataRepo = UserDataRepoImpl(
        ioScope,
        mockAuthRepo,
        mockSahhaApi,
        mockSahhaErrorLogger
    )
    var getDemographic = GetDemographicUseCase(
        repository = userDataRepo
    )

    val mockUserDataInteractionManager = UserDataInteractionManager(
        mainScope = mainScope,
        ioScope = ioScope,
        authRepo = mockAuthRepo,
        deviceInfoRepo = mockDeviceInfoRepo,
        sahhaConfigRepo = mockSahhaConfigRepo,
        getScoresUseCase = getScores,
        getDemographicUseCase = getDemographic,
        postDemographicUseCase = postDemographic,
    )

//    val mockPlatformPermissionProcessor = PlatformPermissionProcessorImpl()

    val saveTokensUseCase = SaveTokensUseCase(
        context = mockk(),
        ioScope = ioScope,
        repository = mockAuthRepo,
        userData = mockUserDataInteractionManager,
    )

    val mutex = Mutex()

    val calculateBatchLimit = CalculateBatchLimit(
        batchedDataRepo = mockBatchedDataRepo
    )

    val postChunkManager = PostChunkManagerImpl(
        mutex = mutex,
    )

    val mockSensorToHealthConnectMetricMapper = SensorToHealthConnectMetricMapper()

    val mockPermissionActionProvider = PermissionActionProviderImpl(
        mockHealthConnectRepo
    )

    val mockBatchAggregateLogs = BatchAggregateLogs(
        timeManager = mockSahhaTimeManager,
        provider = mockPermissionActionProvider,
        repository = mockHealthConnectRepo
    )

    val mockConnectionStateManager = AndroidConnectionStateManager(context = mockk())
    val mockAggregationDataTypeMapper = AggregationDataTypeMapper()
    val mockDataLogTransformer = AggregateDataLogTransformer(mockAggregationDataTypeMapper)
    val mockHealthConnectClient = FakeHealthConnectClient()
}
