//package sdk.sahha.android.domain.model.steps
//
//import junit.framework.TestCase
//import org.junit.Before
//import org.junit.Test
//import sdk.sahha.android.common.Constants
//import sdk.sahha.android.data.remote.dto.send.StepSendDto
//import sdk.sahha.android.di.ManualDependencies
//import sdk.sahha.android.source.Sahha
//import sdk.sahha.android.source.SahhaEnvironment
//
//class StepDataKtTest : TestCase() {
//    @Before
//    override fun setUp() {
//        super.setUp()
//        Sahha.di = ManualDependencies(SahhaEnvironment.development)
//    }
//
//    @Test
//    fun `test step data to step dto`() {
//        val dto = listOf(
//            StepData(
//                source = Constants.STEP_COUNTER_DATA_SOURCE,
//                count = 1000,
//                detectedAt = "1970-01-01T00:00:00+7:00"
//            ).toStepDto("1970-01-01T01:05:00+7:00"),
//            StepData(
//                source = Constants.STEP_COUNTER_DATA_SOURCE,
//                count = 1000,
//                detectedAt = "1970-01-01T01:00:00+7:00"
//            ).toStepDto("1970-01-01T01:05:00+7:00")
//        )
//
//        assertEquals(
//            listOf(
//                StepSendDto(
//                    dataType = "TotalSteps",
//                    count = 1000,
//                    source = Constants.STEP_COUNTER_DATA_SOURCE,
//                    manuallyEntered = false,
//                    startDateTime = "1970-01-01T00:00:00+7:00",
//                    endDateTime = "1970-01-01T00:00:00+7:00",
//                    createdAt = "1970-01-01T01:05:00+7:00"
//                ),
//                StepSendDto(
//                    dataType = "TotalSteps",
//                    count = 1000,
//                    source = Constants.STEP_COUNTER_DATA_SOURCE,
//                    manuallyEntered = false,
//                    startDateTime = "1970-01-01T01:00:00+7:00",
//                    endDateTime = "1970-01-01T01:00:00+7:00",
//                    createdAt = "1970-01-01T01:05:00+7:00"
//                )
//            ),
//            dto
//        )
//    }
//}