//package sdk.sahha.android.suite
//
//import android.app.usage.UsageEvents
//import io.mockk.mockk
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert
//import org.junit.Test
//import sdk.sahha.android.common.Constants
//import sdk.sahha.android.data.mapper.UsageEventMapper
//import sdk.sahha.android.data.repository.AppUsageRepoImpl
//import sdk.sahha.android.domain.model.data_log.SahhaDataLog
//
//class AppUsageFilters {
//    private val repo = AppUsageRepoImpl(
//        usageStatsManager = mockk(),
//        queriedTimeDao = mockk(),
//        scope = mockk()
//    )
//
//    private val testLogs = listOf(
//        SahhaDataLog(
//            id = "normal",
//            logType = "",
//            dataType = "",
//            value = 0.0,
//            source = "com.example.app",
//            startDateTime = "",
//            endDateTime = "",
//            unit = "",
//            recordingMethod = "",
//            deviceType = "",
//            additionalProperties = hashMapOf("eventType" to UsageEventMapper.getString(UsageEvents.Event.ACTIVITY_STOPPED)),
//            parentId = ""
//        ),
//        SahhaDataLog(
//            id = "unknownPackage",
//            logType = "",
//            dataType = "",
//            value = 0.0,
//            source = Constants.UNKNOWN,
//            startDateTime = "",
//            endDateTime = "",
//            unit = "",
//            recordingMethod = "",
//            deviceType = "",
//            additionalProperties = hashMapOf("eventType" to UsageEventMapper.getString(UsageEvents.Event.ACTIVITY_STOPPED)),
//            parentId = ""
//        ),
//        SahhaDataLog(
//            id = "unknownType",
//            logType = "",
//            dataType = "",
//            value = 0.0,
//            source = "com.example.app",
//            startDateTime = "",
//            endDateTime = "",
//            unit = "",
//            recordingMethod = "",
//            deviceType = "",
//            additionalProperties = hashMapOf("eventType" to Constants.UNKNOWN),
//            parentId = ""
//        ),
//        SahhaDataLog(
//            id = "bothUnknown",
//            logType = "",
//            dataType = "",
//            value = 0.0,
//            source = Constants.UNKNOWN,
//            startDateTime = "",
//            endDateTime = "",
//            unit = "",
//            recordingMethod = "",
//            deviceType = "",
//            additionalProperties = hashMapOf("eventType" to Constants.UNKNOWN),
//            parentId = ""
//        ),
//        SahhaDataLog(
//            id = "sourceAndroid",
//            logType = "",
//            dataType = "",
//            value = 0.0,
//            source = "android",
//            startDateTime = "",
//            endDateTime = "",
//            unit = "",
//            recordingMethod = "",
//            deviceType = "",
//            additionalProperties = hashMapOf("eventType" to UsageEventMapper.getString(UsageEvents.Event.ACTIVITY_STOPPED)),
//            parentId = ""
//        ),
//        SahhaDataLog(
//            id = "sourceNexusLauncher",
//            logType = "",
//            dataType = "",
//            value = 0.0,
//            source = "com.google.android.apps.nexuslauncher",
//            startDateTime = "",
//            endDateTime = "",
//            unit = "",
//            recordingMethod = "",
//            deviceType = "",
//            additionalProperties = hashMapOf("eventType" to UsageEventMapper.getString(UsageEvents.Event.ACTIVITY_STOPPED)),
//            parentId = ""
//        ),
//    )
//
//    private val filtered = repo.filterEventLogs(testLogs)
//
//    @Test
//    fun filteredCount_hasOneValidLog() = runTest {
//        println(filtered)
//        Assert.assertEquals(1, filtered.count())
//    }
//
//    @Test
//    fun filteredList_containsOnlyNormalLog() = runTest {
//        println(filtered)
//        Assert.assertEquals(true, filtered.count() == 1 && filtered.first().id == "normal")
//    }
//}