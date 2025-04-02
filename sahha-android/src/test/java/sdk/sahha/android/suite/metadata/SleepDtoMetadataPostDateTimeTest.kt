//package sdk.sahha.android.suite.metadata
//
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert
//import org.junit.Test
//import sdk.sahha.android.di.AppModule
//import sdk.sahha.android.domain.model.metadata.SahhaMetadata
//import sdk.sahha.android.domain.model.dto.SleepDto
//
//class SleepDtoMetadataPostDateTimeTest {
//    @Test
//    fun noPostDateTime_addsPostDateTime() = runTest {
//        val data = listOf(
//            SleepDto(
//                durationInMinutes = 123,
//                startDateTime = "test",
//                endDateTime = "test",
//                source = "test",
//                sleepStage = "test",
//                createdAt = "test",
//                postDateTime = null,
//                id = "test",
//            ),
//        )
//        val modified = AppModule.addMetadata(
//            data,
//            AppModule.mockSleepRepo::saveSleep
//        )
//        modified.forEach { println(it) }
//        Assert.assertEquals(true, modified.all { it.postDateTime != null })
//    }
//
//    @Test
//    fun existingPostDateTime_appendsPostDateTime() = runTest {
//        val data = listOf(
//            SleepDto(
//                durationInMinutes = 123,
//                startDateTime = "test",
//                endDateTime = "test",
//                source = "test",
//                sleepStage = "test",
//                createdAt = "test",
//                postDateTime = "test",
//                id = "test2",
//            ),
//        )
//        val modified = AppModule.addMetadata(
//            data,
//            AppModule.mockSleepRepo::saveSleep
//        )
//        modified.forEach { println(it) }
//        Assert.assertEquals("test", modified.first().postDateTime)
//    }
//}