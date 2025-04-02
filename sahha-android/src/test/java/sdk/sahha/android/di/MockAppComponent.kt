package sdk.sahha.android.di

import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.data.manager.MockIdManager
import sdk.sahha.android.data.mapper.HealthConnectMapperDefaults
import sdk.sahha.android.data.remote.MockSahhaApi
import sdk.sahha.android.data.remote.MockSahhaErrorApi
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.remote.SahhaErrorApi
import sdk.sahha.android.data.repository.MockAuthRepoImpl
import sdk.sahha.android.data.repository.MockBatchedDataRepoImpl
import sdk.sahha.android.data.repository.MockDeviceInfoRepoImpl
import sdk.sahha.android.data.repository.MockDeviceUsageRepoImpl
//import sdk.sahha.android.data.repository.MockHealthConnectRepoImpl
import sdk.sahha.android.data.repository.MockSahhaConfigRepo
import sdk.sahha.android.data.repository.MockSensorRepoImpl
import sdk.sahha.android.data.repository.MockSleepRepoImpl
import sdk.sahha.android.data.repository.MockUserDataRepoImpl
import sdk.sahha.android.data.repository.UserDataRepoImpl
import sdk.sahha.android.domain.interaction.UserDataInteractionManager
import sdk.sahha.android.domain.use_case.GetDemographicUseCase
import sdk.sahha.android.domain.use_case.GetScoresUseCase
import sdk.sahha.android.domain.use_case.SaveTokensUseCase
import sdk.sahha.android.domain.use_case.background.FilterActivityOverlaps
import sdk.sahha.android.domain.use_case.metadata.AddMetadata
import sdk.sahha.android.domain.use_case.post.PostDemographicUseCase
import sdk.sahha.android.framework.MockHealthConnectConstantsMapperImpl

internal class MockAppComponent : AppComponent {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.placeholder.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val behavior = NetworkBehavior.create()

    override val mockRetrofit = MockRetrofit.Builder(retrofit)
        .networkBehavior(behavior)
        .build()

    override val sahhaApi = retrofit.create(SahhaApi::class.java)

    override val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override val defaultScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    override val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    override val delegateApi = mockRetrofit.create(SahhaApi::class.java)

    override val delegateErrorApi = mockRetrofit.create(SahhaErrorApi::class.java)
    override val mockSahhaApi = MockSahhaApi(delegateApi)
    override val mockSahhaErrorApi = MockSahhaErrorApi(delegateErrorApi)
    override val mockSahhaTimeManager = SahhaTimeManager()
    override val mockBatchedDataRepo = MockBatchedDataRepoImpl()
//    override val mockHealthConnectRepo = MockHealthConnectRepoImpl()
    override val mockAuthRepo = MockAuthRepoImpl()
    override val mockDeviceInfoRepo = MockDeviceInfoRepoImpl()
    override val mockSahhaConfigRepo = MockSahhaConfigRepo()
    override val mockUserDataRepo = MockUserDataRepoImpl()
    override val mockDeviceUsageRepo = MockDeviceUsageRepoImpl()
    override val mockSleepRepo = MockSleepRepoImpl()
    override val mockSensorRepo = MockSensorRepoImpl()
    override val mockHealthConnectConstantsMapper = MockHealthConnectConstantsMapperImpl()
    override val mockSahhaErrorLogger = SahhaErrorLogger(
        mockk(),
        mockSahhaErrorApi,
        mainScope,
        mockAuthRepo,
        mockSahhaConfigRepo
    )
    override val mockIdManager = MockIdManager()
    override val filterOverlaps = FilterActivityOverlaps(
        mockBatchedDataRepo, mockSahhaTimeManager
    )
    override val getScores = GetScoresUseCase(
        repository = mockUserDataRepo,
        sahhaTimeManager = mockSahhaTimeManager,
        sahhaErrorLogger = null,
    )

    override val postDemographic = PostDemographicUseCase(
        repository = mockUserDataRepo
    )
    override val addMetadata = AddMetadata(
        timeManager = mockSahhaTimeManager
    )
    override var userDataRepo = UserDataRepoImpl(
        ioScope,
        mockAuthRepo,
        mockSahhaApi,
        mockSahhaErrorLogger
    )
    override var getDemographic = GetDemographicUseCase(
        repository = userDataRepo
    )

    override val mapperDefaults = HealthConnectMapperDefaults(
        mockHealthConnectConstantsMapper,
        mockSahhaTimeManager,
        mockIdManager
    )

    override val mockUserDataInteractionManager: UserDataInteractionManager =
        UserDataInteractionManager(
            mainScope = mainScope,
            ioScope = ioScope,
            authRepo = mockAuthRepo,
            deviceInfoRepo = mockDeviceInfoRepo,
            sahhaConfigRepo = mockSahhaConfigRepo,
            getScoresUseCase = getScores,
            getDemographicUseCase = getDemographic,
            postDemographicUseCase = postDemographic,
        )

    override val saveTokensUseCase = SaveTokensUseCase(
        context = mockk(),
        ioScope = ioScope,
        repository = mockAuthRepo,
        userData = mockUserDataInteractionManager,
    )
}
