//package sdk.sahha.android.di
//
//import kotlinx.coroutines.CoroutineScope
//import retrofit2.mock.BehaviorDelegate
//import retrofit2.mock.MockRetrofit
//import sdk.sahha.android.common.SahhaErrorLogger
//import sdk.sahha.android.common.SahhaTimeManager
//import sdk.sahha.android.data.manager.MockIdManager
//import sdk.sahha.android.data.mapper.HealthConnectMapperDefaults
////import sdk.sahha.android.data.mapper.HealthConnectMapperDefaults
//import sdk.sahha.android.data.remote.MockSahhaApi
//import sdk.sahha.android.data.remote.MockSahhaErrorApi
//import sdk.sahha.android.data.remote.SahhaApi
//import sdk.sahha.android.data.remote.SahhaErrorApi
//import sdk.sahha.android.data.repository.MockAuthRepoImpl
//import sdk.sahha.android.data.repository.MockBatchedDataRepoImpl
//import sdk.sahha.android.data.repository.MockDeviceInfoRepoImpl
//import sdk.sahha.android.data.repository.MockDeviceUsageRepoImpl
////import sdk.sahha.android.data.repository.MockHealthConnectRepoImpl
//import sdk.sahha.android.data.repository.MockSahhaConfigRepo
//import sdk.sahha.android.data.repository.MockSensorRepoImpl
//import sdk.sahha.android.data.repository.MockSleepRepoImpl
//import sdk.sahha.android.data.repository.MockUserDataRepoImpl
//import sdk.sahha.android.data.repository.UserDataRepoImpl
//import sdk.sahha.android.domain.interaction.UserDataInteractionManager
//import sdk.sahha.android.domain.use_case.GetDemographicUseCase
//import sdk.sahha.android.domain.use_case.GetScoresUseCase
//import sdk.sahha.android.domain.use_case.SaveTokensUseCase
//import sdk.sahha.android.domain.use_case.background.FilterActivityOverlaps
//import sdk.sahha.android.domain.use_case.metadata.AddMetadata
//import sdk.sahha.android.domain.use_case.post.PostDemographicUseCase
//import sdk.sahha.android.framework.MockHealthConnectConstantsMapperImpl
//
//internal interface AppComponent {
//    val mockRetrofit: MockRetrofit?
//    val sahhaApi: SahhaApi?
//    val mainScope: CoroutineScope
//    val defaultScope: CoroutineScope
//    val ioScope: CoroutineScope
//    val delegateApi: BehaviorDelegate<SahhaApi>?
//    val delegateErrorApi: BehaviorDelegate<SahhaErrorApi>?
//    val mockSahhaApi: MockSahhaApi
//    val mockSahhaErrorApi: MockSahhaErrorApi
//    val mockSahhaTimeManager: SahhaTimeManager
//    val mockBatchedDataRepo: MockBatchedDataRepoImpl
////    val mockHealthConnectRepo: MockHealthConnectRepoImpl
//    val mockAuthRepo: MockAuthRepoImpl
//    val mockDeviceInfoRepo: MockDeviceInfoRepoImpl
//    val mockSahhaConfigRepo: MockSahhaConfigRepo
//    val mockUserDataRepo: MockUserDataRepoImpl
//    val mockDeviceUsageRepo: MockDeviceUsageRepoImpl
//    val mockSleepRepo: MockSleepRepoImpl
//    val mockSensorRepo: MockSensorRepoImpl
//    val mockHealthConnectConstantsMapper: MockHealthConnectConstantsMapperImpl
//    val mockSahhaErrorLogger: SahhaErrorLogger
//    val mockIdManager: MockIdManager
//    val filterOverlaps: FilterActivityOverlaps
//    val getScores: GetScoresUseCase
//    val postDemographic: PostDemographicUseCase
//    val addMetadata: AddMetadata
//    var userDataRepo: UserDataRepoImpl
//    var getDemographic: GetDemographicUseCase
//    val saveTokensUseCase: SaveTokensUseCase
//    val mockUserDataInteractionManager: UserDataInteractionManager
////    val mapperDefaults: HealthConnectMapperDefaults
//    val mapperDefaults: HealthConnectMapperDefaults
//}