//package sdk.sahha.android.suite.metadata
//
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert
//import org.junit.Test
//import sdk.sahha.android.di.AppModule
//import sdk.sahha.android.domain.model.metadata.SahhaMetadata
//import sdk.sahha.android.domain.model.device.PhoneUsage
//
//class PhoneUsageMetadataPostDateTimeTest {
//    @Test
//    fun noPostDateTime_addsPostDateTime() = runTest {
//        val data = listOf(
//            PhoneUsage(
//                isLocked = true,
//                isScreenOn = true,
//                createdAt = "test",
//                postDateTime = null,
//                id = "test",
//            ),
//        )
//        val modified = AppModule.addMetadata(
//            data,
//            AppModule.mockDeviceUsageRepo::saveUsages
//        )
//        modified.forEach { println(it) }
//        Assert.assertEquals(true, modified.all { it.postDateTime != null })
//    }
//
//    @Test
//    fun existingPostDateTime_appendsPostDateTime() = runTest {
//        val data = listOf(
//            PhoneUsage(
//                isLocked = true,
//                isScreenOn = true,
//                createdAt = "test",
//                postDateTime = "test1",
//                id = "test2",
//            ),
//        )
//        val modified = AppModule.addMetadata(
//            data,
//            AppModule.mockDeviceUsageRepo::saveUsages
//        )
//        modified.forEach { println(it) }
//        Assert.assertEquals("test", modified.first().postDateTime)
//    }
//}