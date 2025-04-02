//package sdk.sahha.android.domain.use_case
//
//import android.os.Build
//import junit.framework.TestCase
//import kotlinx.coroutines.test.runTest
//import org.junit.Before
//import org.junit.Test
//import sdk.sahha.android.common.SahhaErrors
//import sdk.sahha.android.common.SahhaTimeManager
//import sdk.sahha.android.data.repository.FakeSensorRepoImpl
//import java.time.LocalDateTime
//import java.util.*
//
//class AnalyzeProfileUseCaseTest : TestCase() {
//    lateinit var fakeSensorRepo: FakeSensorRepoImpl
//    lateinit var analyzeUseCase: AnalyzeProfileUseCase
//    lateinit var sahhaTimeManager: SahhaTimeManager
//
//    @Before
//    override fun setUp() {
//        super.setUp()
//
//        sahhaTimeManager = SahhaTimeManager()
//        fakeSensorRepo = FakeSensorRepoImpl()
//        analyzeUseCase = AnalyzeProfileUseCase(fakeSensorRepo, sahhaTimeManager)
//    }
//
//    @Test
//    fun `test analyze with no date success`() = runTest {
//        fakeSensorRepo.successful = true
//
//        analyzeUseCase(false) { error, success ->
//            assertEquals("{}", success)
//        }
//    }
//
//    @Test
//    fun `test analyze with no date fail`() = runTest {
//        fakeSensorRepo.successful = false
//
//        analyzeUseCase(false) { error, success ->
//            assertEquals("Failure", error)
//        }
//    }
//
//    @Test
//    fun `test analyze date by date success`() = runTest {
//        fakeSensorRepo.successful = true
//
//        val now = Date()
//        analyzeUseCase(
//            false,
//            dates = Pair(
//                now,
//                now,
//            )
//        ) { error, success ->
//            success?.also {
//                assertEquals(it.contains(sahhaTimeManager.dateToISO(now)), true)
//                assertEquals(it.contains(sahhaTimeManager.dateToISO(now)), true)
//            } ?: assertEquals(SahhaErrors.androidVersionTooLow(7), error)
//        }
//    }
//
//    @Test
//    fun `test analyze date by date fail`() = runTest {
//        fakeSensorRepo.successful = false
//
//        val now = Date()
//        analyzeUseCase(
//            false,
//            dates = Pair(now, now)
//        ) { error, _ ->
//            error?.also {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    assertEquals("Failure", error)
//                } else {
//                    assertEquals(SahhaErrors.androidVersionTooLow(7), it)
//                }
//            }
//        }
//    }
//
//    @Test
//    fun `test analyze date by localDateTime`() = runTest {
//        val now = LocalDateTime.now()
//        analyzeUseCase(
//            false,
//            dates = Pair(
//                now,
//                now,
//            )
//        ) { error, success ->
//            success?.also {
//                assertEquals(it.contains(sahhaTimeManager.localDateTimeToISO(now)), true)
//                assertEquals(it.contains(sahhaTimeManager.localDateTimeToISO(now)), true)
//            } ?: assertEquals(SahhaErrors.androidVersionTooLow(8), error)
//        }
//    }
//}