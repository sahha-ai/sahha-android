//package sdk.sahha.android.suite
//
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.suspendCancellableCoroutine
//import kotlinx.coroutines.test.runTest
//import okhttp3.mockwebserver.MockResponse
//import okhttp3.mockwebserver.MockWebServer
//import org.junit.After
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Test
//import org.mockito.Mockito
//import org.mockito.Mockito.mock
//import retrofit2.Call
//import retrofit2.Response
//import sdk.sahha.android.data.remote.SahhaApi
//import sdk.sahha.android.data.repository.UserDataRepoImpl
//import sdk.sahha.android.di.AppModule
//import sdk.sahha.android.domain.model.dto.DemographicDto
//import sdk.sahha.android.domain.repository.UserDataRepo
//import sdk.sahha.android.domain.use_case.GetDemographicUseCase
//import sdk.sahha.android.source.SahhaDemographic
//import kotlin.coroutines.resume
//
//class NullDemographicsTest {
//    private lateinit var repository: UserDataRepo
//    private lateinit var mockCall: Call<DemographicDto>
//
//    @Before
//    fun setup() {
//        // Initialize mocks
//        mockCall = mock(Call::class.java) as Call<DemographicDto>
//
//        // Use mockSahhaApi in userDataRepo
//        AppModule.userDataRepo = UserDataRepoImpl(
//            AppModule.ioScope,
//            AppModule.mockAuthRepo,
//            AppModule.mockSahhaApi, // Use the mocked API
//            AppModule.mockSahhaErrorLogger
//        )
//
//        // Update getDemographic use case
//        AppModule.getDemographic = GetDemographicUseCase(
//            repository = AppModule.userDataRepo
//        )
//    }
//
//    @Test
//    fun initialGet_returnsDemographicsWithNullParams_noErrors() = runTest {
//        val response = Response.success(
//            DemographicDto(
//                null, null, null, null, null, null, null, null, null, null, null, null, null
//            )
//        )
//
//        // Mock the API call
//        Mockito.`when`(AppModule.mockSahhaApi.getDemographic("profile_token")).thenReturn(mockCall)
//        Mockito.`when`(mockCall.execute()).thenReturn(response)
//
//        suspendCancellableCoroutine<Unit> { cont ->
//            AppModule.ioScope.launch {
//                AppModule.getDemographic { error, demographic ->
//                    Assert.assertNull(error)
//                    println(demographic)
//                    Assert.assertEquals(SahhaDemographic(), demographic)
//                    if (cont.isActive) cont.resume(Unit)
//                }
//            }
//        }
//    }
//}