//package sdk.sahha.android.suite.batched
//
//import androidx.activity.ComponentActivity
//import androidx.health.connect.client.records.ExerciseLap
//import androidx.health.connect.client.records.ExerciseSegment
//import androidx.health.connect.client.records.ExerciseSessionRecord
//import androidx.health.connect.client.records.metadata.DataOrigin
//import androidx.health.connect.client.records.metadata.Metadata
//import androidx.health.connect.client.request.ReadRecordsRequest
//import androidx.health.connect.client.time.TimeRangeFilter
//import androidx.health.connect.client.units.Length
//import androidx.test.core.app.ApplicationProvider
//import kotlinx.coroutines.CompletableDeferred
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert
//import org.junit.BeforeClass
//import org.junit.Test
//import sdk.sahha.android.common.SahhaSetupUtil
//import sdk.sahha.android.common.appId
//import sdk.sahha.android.common.appSecret
//import sdk.sahha.android.common.externalId
//import sdk.sahha.android.data.mapper.toSahhaDataLogDto
//import sdk.sahha.android.source.Sahha
//import sdk.sahha.android.source.SahhaConverterUtility
//import sdk.sahha.android.source.SahhaEnvironment
//import sdk.sahha.android.source.SahhaSettings
//import java.time.ZonedDateTime
//import kotlin.coroutines.resume
//import kotlin.coroutines.suspendCoroutine
//
//class ExerciseSessionTest {
//    companion object {
//        lateinit var application: Application
//
//        @BeforeClass
//        @JvmStatic
//        fun beforeClass() = runTest {
//            activity = ApplicationProvider.getApplicationContext()
//            val settings = SahhaSettings(environment = SahhaEnvironment.sandbox)
//            SahhaSetupUtil.configureSahha(activity, settings)
//            SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
////            suspendCoroutine { cont ->
////                Sahha.enableSensors(application) { error, status ->
////                    error?.also { throw Exception(it) }
////                    println(status.name)
////                    cont.resume(Unit)
////                }
////            }
//        }
//    }
//
//    @Test
//    fun exerciseSession_mapsCorrectly() = runTest{
//        val sessions = generateExerciseSessions(1)
//        val batched = sessions.flatMap { exercise ->
//            listOf(exercise.toSahhaDataLogDto()) +
//                    exercise.laps.map { lap -> lap.toSahhaDataLogDto(exercise) } +
//                    exercise.segments.map { segment ->
//                        segment.toSahhaDataLogDto(
//                            exercise
//                        )
//                    }
//        }
//        batched.forEach { b ->
//            println(SahhaConverterUtility.convertToJsonString(b))
//        }
//    }
//
//
////    @Test
////    fun sessions_sendNestedList() = runTest {
////        val records = generateExerciseSessions(3)
////        Sahha.sim.sensor.batchDataLogs()
////        val batched = Sahha.di.batchedDataRepo.getBatchedData()
////
////        val deferredResult = CompletableDeferred<Boolean>()
////
////        Sahha.sim.sensor.postBatchData(application.applicationContext, batched) { error, successful ->
////            println(error)
////            println(successful)
////            deferredResult.complete(successful)
////        }
////
////        val result = deferredResult.await()
////        Assert.assertEquals(true, result)
//////        Assert.assertEquals(3, Sahha.di.postChunkManager.postedChunkCount)
////    }
////
////    @Test
////    fun insertRecords() = runTest {
////        Sahha.di.healthConnectClient?.also { client ->
////            val records = generateExerciseSessions(3)
////            val insert = client.insertRecords(records)
////            println(insert.recordIdsList)
////        }
////    }
////
////    @Test
////    fun deleteRecords() = runTest {
////        Sahha.di.healthConnectClient?.also { client ->
////            val response = client.readRecords<ExerciseSessionRecord>(
////                ReadRecordsRequest(
////                    TimeRangeFilter.before(ZonedDateTime.now().toInstant())
////                )
////            )
////            val toDelete =
////                response.records.filter { it.metadata.dataOrigin.packageName == "sdk.sahha.android.test" }
////            client.deleteRecords(
////                ExerciseSessionRecord::class,
////                toDelete.map { it.metadata.id },
////                toDelete.map { it.metadata.clientRecordId ?: "" })
////        }
////    }
//
//    private fun generateExerciseSessions(
//        sessionAmount: Int
//    ): List<ExerciseSessionRecord> {
//        val sessions = mutableListOf<ExerciseSessionRecord>()
//        var lastStart = ZonedDateTime.now().minusMinutes((sessionAmount * 20).toLong())
//        var lastEnd = lastStart.plusMinutes(10)
//
//        for (i in 0 until sessionAmount) {
//            sessions.add(
//                ExerciseSessionRecord(
//                    startTime = lastStart.toInstant(),
//                    startZoneOffset = lastStart.offset,
//                    endTime = lastEnd.toInstant(),
//                    endZoneOffset = lastEnd.offset,
//                    exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS,
//                    segments = listOf(
//                        ExerciseSegment(
//                            lastStart.toInstant(),
//                            lastStart.toInstant().plusSeconds(30),
//                            ExerciseSegment.EXERCISE_SEGMENT_TYPE_PULL_UP,
//                            10
//                        ),
//                        ExerciseSegment(
//                            lastStart.toInstant().plusSeconds(30),
//                            lastStart.toInstant().plusSeconds(60),
//                            ExerciseSegment.EXERCISE_SEGMENT_TYPE_CRUNCH,
//                            10
//                        ),
//                        ExerciseSegment(
//                            lastStart.toInstant().plusSeconds(60),
//                            lastStart.toInstant().plusSeconds(90),
//                            ExerciseSegment.EXERCISE_SEGMENT_TYPE_DEADLIFT,
//                            10
//                        )
//                    ),
//                    laps = listOf(
//                        ExerciseLap(
//                            lastStart.toInstant().plusSeconds(90),
//                            lastStart.toInstant().plusSeconds(120),
//                            Length.meters(100.0)
//                        ),
//                        ExerciseLap(
//                            lastStart.toInstant().plusSeconds(120),
//                            lastStart.toInstant().plusSeconds(150),
//                            Length.meters(100.0)
//                        ),
//                        ExerciseLap(
//                            lastStart.toInstant().plusSeconds(150),
//                            lastStart.toInstant().plusSeconds(180),
//                            Length.meters(100.0)
//                        ),
//                        ExerciseLap(
//                            lastStart.toInstant().plusSeconds(180),
//                            lastStart.toInstant().plusSeconds(210),
//                            Length.meters(100.0)
//                        ),
//                        ExerciseLap(
//                            lastStart.toInstant().plusSeconds(210),
//                            lastStart.toInstant().plusSeconds(240),
//                            Length.meters(100.0)
//                        ),
//                    ),
//                    metadata = Metadata(
//                        id = "PARENT_ID",
//                        clientRecordId = "sahha_test_id_$i",
//                        dataOrigin = DataOrigin(
//                            packageName = "TEST.PACKAGE.NAME"
//                        )
//                    )
//                )
//            )
//
//            lastStart = lastEnd
//            lastEnd = lastEnd.plusMinutes(10)
//        }
//
//        println(sessions.map { "${ZonedDateTime.ofInstant(it.startTime, it.startZoneOffset)}" })
//        println(sessions.map { "${ZonedDateTime.ofInstant(it.endTime, it.endZoneOffset)}" })
//
//        return sessions
//    }
//}