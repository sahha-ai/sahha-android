//package sdk.sahha.android.suite.metadata
//
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert
//import org.junit.Test
//import sdk.sahha.android.di.AppModule
//import sdk.sahha.android.domain.model.metadata.SahhaMetadata
//import sdk.sahha.android.domain.model.steps.StepSession
//
//class StepSessionMetadataPostDateTimeTest {
//    @Test
//    fun noPostDateTime_addsPostDateTime() = runTest {
//        val data = listOf(
//            StepSession(
//                100,
//                "test",
//                "test",
//                null,
//                "test",
//            ),
//        )
//        val modified = AppModule.addMetadata(
//            data,
//            AppModule.mockSensorRepo::saveStepSessions
//        )
//        modified.forEach { println(it) }
//        Assert.assertEquals(true, modified.all { it.postDateTime != null })
//    }
//
//    @Test
//    fun existingPostDateTime_appendsPostDateTime() = runTest {
//        val data = listOf(
//            StepSession(
//                100,
//                "test",
//                "test",
//                "test",
//                "test2",
//            ),
//        )
//        val modified = AppModule.addMetadata(
//            data,
//            AppModule.mockSensorRepo::saveStepSessions
//        )
//        modified.forEach { println(it) }
//        Assert.assertEquals("test", modified.first().postDateTime)
//    }
//}