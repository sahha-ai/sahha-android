//package sdk.sahha.android.suite.metadata
//
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert
//import org.junit.Test
//import sdk.sahha.android.di.AppModule
//import sdk.sahha.android.domain.model.data_log.SahhaDataLog
//import java.util.UUID
//
//class SahhaDataLogMetadataPostDateTimeTest {
//    private val deviceId = UUID.randomUUID().toString()
//
//    @Test
//    fun noPostDateTime_addsPostDateTime() = runTest {
//        val batchedData = listOf(
//            SahhaDataLog(
//                id = "test",
//                logType = "test",
//                dataType = "test",
//                value = 100.0,
//                source = "test",
//                startDateTime = "test",
//                endDateTime = "test",
//                unit = "test",
//                recordingMethod = "test",
//                deviceId = deviceId,
//                deviceType = "test",
//                additionalProperties = null,
//                postDateTime = null,
//                parentId = null,
//            ),
//        )
//        val modified = AppModule.addMetadata(
//            batchedData,
//            AppModule.mockBatchedDataRepo::saveBatchedData
//        )
//        modified.forEach { println(it) }
//        Assert.assertEquals(true, modified.all { it.postDateTime != null })
//    }
//
//    @Test
//    fun existingPostDateTime_appendsPostDateTime() = runTest {
//        val batchedData = listOf(
//            SahhaDataLog(
//                id = "test2",
//                logType = "test",
//                dataType = "test",
//                value = 200.0,
//                source = "test",
//                startDateTime = "test",
//                endDateTime = "test",
//                unit = "test",
//                recordingMethod = "test",
//                deviceId = deviceId,
//                deviceType = "test",
//                additionalProperties = null,
//                postDateTime = "test",
//                parentId = null,
//            )
//        )
//
//        val modified = AppModule.addMetadata(
//            batchedData,
//            AppModule.mockBatchedDataRepo::saveBatchedData
//        )
//        modified.forEach { println(it) }
//        Assert.assertEquals("test", modified.first().postDateTime)
//    }
//}