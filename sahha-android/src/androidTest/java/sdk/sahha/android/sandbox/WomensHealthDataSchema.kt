//package sdk.sahha.android.sandbox
//
//import androidx.activity.ComponentActivity
//import androidx.health.connect.client.records.CervicalMucusRecord
//import androidx.health.connect.client.records.IntermenstrualBleedingRecord
//import androidx.health.connect.client.records.MenstruationFlowRecord
//import androidx.health.connect.client.records.MenstruationPeriodRecord
//import androidx.health.connect.client.records.OvulationTestRecord
//import androidx.health.connect.client.records.SexualActivityRecord
//import androidx.health.connect.client.records.metadata.DataOrigin
//import androidx.health.connect.client.records.metadata.Metadata
//import androidx.test.core.app.ApplicationProvider
//import kotlinx.coroutines.test.runTest
//import org.junit.BeforeClass
//import org.junit.Test
//import sdk.sahha.android.common.SahhaSetupUtil
//import sdk.sahha.android.data.mapper.toSahhaDataLogDto
//import sdk.sahha.android.source.SahhaConverterUtility
//import sdk.sahha.android.source.SahhaEnvironment
//import sdk.sahha.android.source.SahhaSettings
//import java.io.File
//import java.time.ZonedDateTime
//
//class WomensHealthDataSchema {
//    companion object {
//        private val application: activity = ApplicationProvider.getApplicationContext()
//
//        @JvmStatic
//        @BeforeClass
//        fun before() = runTest {
//            val settings = SahhaSettings(
//                environment = SahhaEnvironment.sandbox
//            )
//            SahhaSetupUtil.configureSahha(
//                application,
//                settings
//            )
//        }
//    }
//
//    val converter = SahhaConverterUtility
//
//    private fun writeToFile(
//        jsonString: String,
//        fileName: String,
//        fileExtension: String = ".json",
//    ) {
//        // Get the internal storage directory
//        val file = File(application.filesDir, "$fileName$fileExtension")
//
//        // Write the JSON string to a file in internal storage
//        file.writeText(jsonString)
//    }
//
//    @Test
//    fun cervicalMucusMapping() = runTest {
//        val now = ZonedDateTime.now()
//        val record = CervicalMucusRecord(
//            now.toInstant(),
//            now.offset,
//            CervicalMucusRecord.APPEARANCE_UNUSUAL,
//            CervicalMucusRecord.SENSATION_MEDIUM,
//            metadata = Metadata(
//                id = "EXAMPLE_TEST_ID",
//                recordingMethod = Metadata.RECORDING_METHOD_MANUAL_ENTRY,
//                dataOrigin = DataOrigin("com.test.app")
//            )
//        )
//
//        val log = record.toSahhaDataLogDto()
//        val converted = converter.convertToJsonString(log)
//
//        println(converted)
//        writeToFile(converted, "cervical-mucus")
//    }
//
//    @Test
//    fun intermenstrualBleedingMapping() = runTest {
//        val now = ZonedDateTime.now()
//        val record = IntermenstrualBleedingRecord(
//            now.toInstant(),
//            now.offset,
//            metadata = Metadata(
//                id = "EXAMPLE_TEST_ID",
//                recordingMethod = Metadata.RECORDING_METHOD_MANUAL_ENTRY,
//                dataOrigin = DataOrigin("com.test.app")
//            )
//        )
//
//        val log = record.toSahhaDataLogDto()
//        val converted = converter.convertToJsonString(log)
//
//        println(converted)
//        writeToFile(converted, "intermenstrual-bleeding")
//    }
//
//    @Test
//    fun menstruationFlowMapping() = runTest {
//        val now = ZonedDateTime.now()
//        val record = MenstruationFlowRecord(
//            now.toInstant(),
//            now.offset,
//            MenstruationFlowRecord.FLOW_MEDIUM,
//            metadata = Metadata(
//                id = "EXAMPLE_TEST_ID",
//                recordingMethod = Metadata.RECORDING_METHOD_MANUAL_ENTRY,
//                dataOrigin = DataOrigin("com.test.app")
//            )
//        )
//
//        val log = record.toSahhaDataLogDto()
//        val converted = converter.convertToJsonString(log)
//
//        println(converted)
//        writeToFile(converted, "menstruation-flow")
//    }
//
//    @Test
//    fun menstruationPeriodMapping() = runTest {
//        val now = ZonedDateTime.now()
//        val record = MenstruationPeriodRecord(
//            now.minusDays(7).toInstant(),
//            now.offset,
//            now.toInstant(),
//            now.offset,
//            metadata = Metadata(
//                id = "EXAMPLE_TEST_ID",
//                recordingMethod = Metadata.RECORDING_METHOD_MANUAL_ENTRY,
//                dataOrigin = DataOrigin("com.test.app")
//            )
//        )
//
//        val log = record.toSahhaDataLogDto()
//        val converted = converter.convertToJsonString(log)
//
//        println(converted)
//        writeToFile(converted, "menstruation-period")
//    }
//
//    @Test
//    fun ovulationTestMapping() = runTest {
//        val now = ZonedDateTime.now()
//        val record = OvulationTestRecord(
//            now.toInstant(),
//            now.offset,
//            result = OvulationTestRecord.RESULT_POSITIVE,
//            metadata = Metadata(
//                id = "EXAMPLE_TEST_ID",
//                recordingMethod = Metadata.RECORDING_METHOD_MANUAL_ENTRY,
//                dataOrigin = DataOrigin("com.test.app")
//            )
//        )
//
//        val log = record.toSahhaDataLogDto()
//        val converted = converter.convertToJsonString(log)
//
//        println(converted)
//        writeToFile(converted, "ovulation-test")
//    }
//
//    @Test
//    fun sexualActivityMapping() = runTest {
//        val now = ZonedDateTime.now()
//        val record = SexualActivityRecord(
//            now.toInstant(),
//            now.offset,
//            protectionUsed = SexualActivityRecord.PROTECTION_USED_PROTECTED,
//            metadata = Metadata(
//                id = "EXAMPLE_TEST_ID",
//                recordingMethod = Metadata.RECORDING_METHOD_MANUAL_ENTRY,
//                dataOrigin = DataOrigin("com.test.app")
//            )
//        )
//
//        val log = record.toSahhaDataLogDto()
//        val converted = converter.convertToJsonString(log)
//
//        println(converted)
//        writeToFile(converted, "sexual-activity")
//    }
//
//    @Test
//    fun printEnums() = runTest {
//        val allEntries = mutableListOf<String>()
//        allEntries.add("Cervical Mucus - Sensation\n\n")
//        CervicalMucusRecord.SENSATION_INT_TO_STRING_MAP.entries.mapTo(
//            allEntries
//        ) { entry -> entry.value }
//        allEntries.add("\n\nCervical Mucus - Appearance")
//        CervicalMucusRecord.APPEARANCE_INT_TO_STRING_MAP.entries.mapTo(
//            allEntries
//        ) { entry -> entry.value }
//        allEntries.add("\n\nMenstruation Flow - Type")
//        MenstruationFlowRecord.FLOW_TYPE_INT_TO_STRING_MAP.entries.mapTo(
//            allEntries
//        ) { entry -> entry.value }
//        allEntries.add("\n\nOvaulation Test - Result")
//        OvulationTestRecord.RESULT_INT_TO_STRING_MAP.entries.mapTo(
//            allEntries
//        ) { entry -> entry.value }
//        allEntries.add("\n\nSexual Activity - Protection Used")
//        SexualActivityRecord.PROTECTION_USED_INT_TO_STRING_MAP.entries.mapTo(
//            allEntries
//        ) { entry -> entry.value }
//
//        var allString = ""
//        allEntries.forEach {
//            allString += "$it\n"
//        }
//
//        writeToFile(allString, "enums", ".txt")
//    }
//}