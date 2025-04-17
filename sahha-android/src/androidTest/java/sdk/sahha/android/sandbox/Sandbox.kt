package sdk.sahha.android.sandbox

import android.app.usage.UsageEvents.Event
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.icu.text.DateFormat
import android.icu.util.ULocale
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.changes.UpsertionChange
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyTemperatureMeasurementLocation
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.ExerciseLap
import androidx.health.connect.client.records.ExerciseRoute
import androidx.health.connect.client.records.ExerciseSegment
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.DeviceTypes
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Length
import androidx.health.connect.client.units.Percentage
import androidx.health.connect.client.units.Pressure
import androidx.health.connect.client.units.Temperature
import androidx.test.core.app.ApplicationProvider
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import retrofit2.Response
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.common.SahhaSetupUtil
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.appId
import sdk.sahha.android.common.appSecret
import sdk.sahha.android.common.externalId
import sdk.sahha.android.data.mapper.toSahhaDataLogDto
import sdk.sahha.android.data.repository.SensorRepoImpl
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.di.DaggerAppComponent
import sdk.sahha.android.domain.internal_enum.RecordingMethods
import sdk.sahha.android.domain.model.config.toSahhaSensorSet
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaConverterUtility
import sdk.sahha.android.source.SahhaDemographic
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus
import sdk.sahha.android.source.SahhaSettings
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class Sandbox {
    companion object {
        lateinit var activity: ComponentActivity

        @BeforeClass
        @JvmStatic
        fun beforeClass() = runTest {
            activity = ApplicationProvider.getApplicationContext()
            val settings = SahhaSettings(environment = SahhaEnvironment.sandbox)
            SahhaSetupUtil.configureSahha(activity, settings)
//            SahhaSetupUtil.authenticateSahha(appId, appSecret, "${externalId}_${LocalDate.now()}")
            SahhaSetupUtil.authenticateSahha(appId, appSecret, "min delay test")
            SahhaSetupUtil.enableSensors(
                activity,
                SahhaSensor.values().toSet()
            )
//            activity.startActivity(Intent(activity, RequestAllPermissionsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
//            Sahha.di.batchDataLogs()
        }
    }

    private val list = mutableMapOf<String, String>()
    private val tm = SahhaTimeManager()
    private var calendar: android.icu.util.Calendar? = null
    private val instant: Instant get() = calendar?.time?.toInstant() ?: Instant.now()
    private val time: String get() = tm.instantToIsoTime(instant)
    private var year = -1
    private var month = -1
    private var day = -1
    private var hour = -1
    private var minute = -1

    @Test
    fun postSingleLog() = runTest {
        val manager = SahhaTimeManager()
        val repository = Sahha.di.batchedDataRepo
        repository.saveBatchedData(
            listOf(
                SahhaDataLog(
                    id = UUID.randomUUID().toString(),
                    logType = Constants.DataLogs.SLEEP,
                    dataType = SahhaSensor.sleep.name,
                    value = 12.34,
                    source = "com.example.app",
                    startDateTime = manager.localDateTimeToISO(LocalDateTime.now()),
                    endDateTime = manager.localDateTimeToISO(LocalDateTime.now()),
                    unit = Constants.DataUnits.MINUTE,
                    recordingMethod = RecordingMethods.unknown.name,
                    deviceId = UUID.randomUUID().toString(),
                    deviceType = DeviceTypes.UNKNOWN,
                    additionalProperties = hashMapOf(),
                    parentId = UUID.randomUUID().toString(),
                    postDateTimes = arrayListOf(manager.localDateTimeToISO(LocalDateTime.now())) ,
                )
            )
        )

        suspendCancellableCoroutine { cont ->
            CoroutineScope(Dispatchers.Default).launch {
                Sahha.di.postHealthConnectDataUseCase { error, successful ->
                    println(error)
                    println(successful)
                    cont.resume(Unit)
                }
            }
        }
    }

    @Test
    fun test_period2() = runTest {
        val repo = Sahha.di.healthConnectRepo
        val records = repo.getAggregateRecordsByPeriod(
            metrics = setOf(StepsRecord.COUNT_TOTAL),
            TimeRangeFilter.Companion.between(
                startTime = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.MIDNIGHT
                ),
                endTime = LocalDateTime.of(
                    LocalDate.now().plusDays(1),
                    LocalTime.MIDNIGHT
                )
            ),
            Period.ofDays(1),
        )
        val text = SahhaConverterUtility.convertToJsonString(records)
        print(text)
    }

    @Test
    fun auth() = runTest {
        for (i in 0..1) {
            Sahha.authenticate("", "", "") { error, success ->
                println(error)
                println(success)
            }
        }
        delay(10000)
    }

//    @Test
//    fun postData() = runTest {
//        suspendCancellableCoroutine { cont ->
//            Sahha.di.defaultScope.launch {
//                val data = Sahha.di.batchedDataRepo.getBatchedData()
//                println(data)
//                Sahha.di.postBatchData(
//                    data.subList(0, 10)
//                ) { error, successful ->
//                    Assert.assertEquals(null, error)
//                    Assert.assertEquals(true, successful)
//                    if (cont.isActive) cont.resume(Unit)
//                }
//            }
//        }
//    }

    @Test
    fun device_permissions() = runTest {
        suspendCancellableCoroutine { cont ->
            Sahha.getSensorStatus(activity, setOf(SahhaSensor.device_lock)) { error, status ->
                error?.also { e -> println(e) }
                println("${SahhaSensor.device_lock}: ${status.name}")
                if (cont.isActive) cont.resume(Unit)
            }
        }
    }

    @Test
    fun permissions() = runTest {
        val context: Context = ApplicationProvider.getApplicationContext()
        val jobs = mutableListOf<Job>()
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        SahhaSensor.values().forEach { sensor ->
            jobs += scope.launch {
                suspendCancellableCoroutine { cont ->
                    Sahha.getSensorStatus(context, setOf(sensor)) { error, status ->
                        error?.also { e -> println(e) }
                        println("${sensor.name}: ${status.name}")
                        if (cont.isActive) cont.resume(Unit)
                    }
                }
            }
        }
        jobs.joinAll()
    }

    @Test
    fun nativeSleep() = runTest {
        val now = ZonedDateTime.now()
        val time = SahhaTimeManager()
        val sleepDto = SleepDto(
            durationInMinutes = 1234,
            startDateTime = time.localDateTimeToISO(
                now.minusMinutes(1234).toLocalDateTime(),
                now.zone
            ),
            endDateTime = time.localDateTimeToISO(now.toLocalDateTime(), now.zone),
            source = "manual.test.example",
            sleepStage = Constants.SLEEP_STAGE_IN_BED,
            createdAt = time.localDateTimeToISO(now.toLocalDateTime(), now.zone),
            postDateTimes = null,
        )
        val json = logJsonString("", sleepDto.toSahhaDataLogDto())
        println(json)
    }

    @Test
    fun demog() = runTest {
        suspendCancellableCoroutine { cont ->
            Sahha.postDemographic(SahhaDemographic()) { error, success ->
                println(error)
                println(success)

                Sahha.getDemographic { _, demographic ->
                    println(demographic)
                    if (cont.isActive) cont.resume(Unit)
                }
            }
        }
    }

    @Test
    fun sleepDataTypes() = runTest {
        val records = Sahha.di.healthConnectRepo.getRecords(
            SleepSessionRecord::class,
            TimeRangeFilter.after(LocalDateTime.now().minusDays(1))
        )

        val dataType = records?.first()?.toSahhaDataLogDto()?.dataType

        println(dataType)
        Assert.assertEquals("sleep_stage_in_bed", dataType)
    }

    @Test
    fun sleepRecords() = runTest {
        val records = Sahha.di.healthConnectRepo.getRecords(
            SleepSessionRecord::class,
            TimeRangeFilter.Companion.before(Instant.now())
        )

        records?.forEach { r ->
            println("title:\t\t\t${r.title}")
            println("start:\t\t\t${r.startTime}")
            println("start offset:\t${r.startZoneOffset}")
            println("end:\t\t\t${r.endTime}")
            println("end offset:\t${r.endZoneOffset}")
            r.stages.forEach { stage ->
                println("stage:\t${Sahha.di.healthConnectConstantsMapper.sleepStages(stage.stage)}")
                println("stage start:\t${stage.startTime}")
                println("stage end:\t${stage.endTime}")
            }
            println("\n\n")
        }
    }

    @Test
    fun metaIds() = runTest {
        suspendCancellableCoroutine { cont ->
            val metaIds = runBlocking { Sahha.di.batchedDataRepo.getBatchedData().map { it.id } }
            metaIds.forEach { id ->
                val filtered = metaIds.filter { id == it }
                print("Meta ID exists: ${filtered.count() > 1}\t")
                println(id)
//                Assert.assertEquals(false, metaExists)
            }
            if (cont.isActive) cont.resume(Unit)
        }
    }

    @Test
    fun environment() = runTest {
        var sahhaSettings = SahhaSettings(SahhaEnvironment.sandbox)
        var di = DaggerAppComponent.builder()
            .appModule(AppModule(sahhaSettings.environment))
            .context(activity)
            .build()

        var authSuccess = suspendCancellableCoroutine<Boolean> { cont ->
            Sahha.authenticate(
                appId, appSecret, "test"
            ) { error, success ->
                if (cont.isActive) cont.resume(success)
            }
        }
        Assert.assertEquals(true, authSuccess)

        sahhaSettings = SahhaSettings(SahhaEnvironment.production)
        di = DaggerAppComponent.builder()
            .appModule(AppModule(sahhaSettings.environment))
            .context(activity)
            .build()

        authSuccess = suspendCancellableCoroutine<Boolean> { cont ->
            Sahha.authenticate(
                appId, appSecret, "test"
            ) { error, success ->
                Assert.assertEquals(true, success)
                if (cont.isActive) cont.resume(success)
            }
        }
        Assert.assertEquals(false, authSuccess)
    }

    @Test
    fun reconfigureEnv() = runTest {
        val prefs = getPrefs(ApplicationProvider.getApplicationContext())
        var envInt = prefs.getInt(Constants.ENVIRONMENT_KEY, -1)
        var env = SahhaEnvironment.values()[envInt]
        Assert.assertEquals(SahhaEnvironment.sandbox, env)
        println("Should be sandbox: $env")

        SahhaReconfigure(activity, SahhaEnvironment.production)
        envInt = prefs.getInt(Constants.ENVIRONMENT_KEY, -1)
        env = SahhaEnvironment.values()[envInt]
        Assert.assertEquals(SahhaEnvironment.production, env)
        println("Should be prod: $env")

        SahhaReconfigure(activity, SahhaEnvironment.sandbox)
        envInt = prefs.getInt(Constants.ENVIRONMENT_KEY, -1)
        env = SahhaEnvironment.values()[envInt]
        Assert.assertEquals(SahhaEnvironment.sandbox, env)
        println("Should be sandbox: $env")
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(Constants.CONFIGURATION_PREFS, Context.MODE_PRIVATE)
    }

    @Test
    fun initialConfig() = runTest {
        Sahha.di.configurationDao.clearConfig()
        val config = Sahha.di.sahhaConfigRepo.getConfig()?.sensorArray?.toSahhaSensorSet()
        println(config.toString())
        Assert.assertEquals(null, config)
    }

    @Test
    fun versionCheck() = runTest {
        val before = Sahha.di.sahhaInteractionManager.userData.checkAndResetSensors(
            "0.15.16",
            Sahha.di.sahhaConfigRepo.getConfig()
        )
        Assert.assertEquals(true, before)

        val same = Sahha.di.sahhaInteractionManager.userData.checkAndResetSensors(
            "0.15.17",
            Sahha.di.sahhaConfigRepo.getConfig()
        )
        Assert.assertEquals(false, same)

        val after = Sahha.di.sahhaInteractionManager.userData.checkAndResetSensors(
            "0.15.18",
            Sahha.di.sahhaConfigRepo.getConfig()
        )
        Assert.assertEquals(false, after)
    }

    @Test
    fun token() = runTest {
        SahhaSetupUtil.deauthenticateSahha()
        Assert.assertEquals(null, Sahha.profileToken)
        println("****************************************")
        println(Sahha.profileToken)
        println("****************************************")

        SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
        println("****************************************")
        println(Sahha.profileToken)
        println("****************************************")
        Assert.assertNotNull(Sahha.profileToken)
    }

    @Test
    fun runtimeManifestPermissions() = runTest {
        try {
            // Get the package name of the app and retrieve the PackageInfo with permissions
            val packageName = activity.packageName
            val packageInfo = activity.packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_PERMISSIONS
            )

            // Retrieve the array of permissions
            val requestedPermissions = packageInfo.requestedPermissions

            if (requestedPermissions != null) {
                for (permission in requestedPermissions) {
                    // Print or use the permissions as needed
                    println("Declared Permission: $permission")
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    @Test
    fun openAppSettings() = runTest {
        Sahha.openAppSettings(activity)
    }

    @Test
    fun test_changes() = runTest {
        val repo = Sahha.di.healthConnectRepo
        val changes = repo.getChangedRecords(StepsRecord::class)

        changes?.forEach {
            println(it)
        }
        Assert.assertEquals(true, changes?.isNotEmpty())
    }

//    @Test
//    fun test_timestamps() = runTest {
//        val batchDataLogs = Sahha.sim.sensor.batchDataLogs
//        val repo = Sahha.di.healthConnectRepo
//        batchDataLogs()
//        delay(5000)
//        val sample = repo.getAllStepsHc().find { it.source == Constants.SAMSUNG_HEALTH_PACKAGE_NAME }
//        sample?.also { s ->
//            println(s)
//            val isTotalDayLog = batchDataLogs.isTotalDayTimestamps(s)
//            println(isTotalDayLog)
//            Assert.assertEquals(true, isTotalDayLog)
//        }
//        Assert.assertNotNull(sample)
//    }

    @Test
    fun write2500StepRecords() = runTest {
        val client = Sahha.di.healthConnectClient ?: throw Error("Client is null")
        val records = createMockStepsRecords(25000)
        client.insertRecords(records)
    }

    private fun createMockStepsRecords(amount: Int): List<StepsRecord> {
        val now = ZonedDateTime.now()
        var steps = listOf<StepsRecord>()
        var a = amount.toLong()
        for (i in 1 until amount + 1) {
            steps += listOf(
                StepsRecord(
                    startTime = now.minusMinutes(a).toInstant(),
                    startZoneOffset = now.offset,
                    endTime = now.minusMinutes(--a).toInstant(),
                    endZoneOffset = now.offset,
                    count = i.toLong()
                )
            )
        }
        return steps
    }

    @Test
    fun usages_test() = runTest {
        val usc = activity.getSystemService("usagestats") as UsageStatsManager
        val permissionsIntent =
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        activity.startActivity(permissionsIntent)
        val query = usc.queryEvents(
//            UsageStatsManager.INTERVAL_DAILY,
            ZonedDateTime.of(
                LocalDate.now().minusDays(7),
                LocalTime.of(0, 0),
                ZoneId.systemDefault()
            ).toInstant().toEpochMilli(),
            ZonedDateTime.of(
                LocalDate.now().minusDays(1),
                LocalTime.of(23, 59, 59, 99),
                ZoneId.systemDefault()
            ).toInstant().toEpochMilli(),
        )
        while (query.hasNextEvent()) {
            val event = Event()
            query.getNextEvent(event)
            println("packageName: " + event.packageName)
            println("eventType: ${event.eventType.toEventTypeString()}")
            println("timeStamp: ${event.timeStamp.toZdt()}")
            println("appStandbyBucket: ${event.appStandbyBucket}")
            println("className: " + event.className)
            println("configuration: " + SahhaConverterUtility.convertToJsonString(event.configuration))
            println("lastEventTime: " + event.shortcutId)
            println("\n\n")
        }
    }

    fun Int.toAppStandbyBucketString(): String {
        return ""
    }

    fun Int.toEventTypeString(): String {
        return when (this) {
            Event.ACTIVITY_PAUSED -> "ACTIVITY_PAUSED"
            Event.ACTIVITY_RESUMED -> "ACTIVITY_RESUMED"
            Event.ACTIVITY_STOPPED -> "ACTIVITY_STOPPED"
            Event.DEVICE_SHUTDOWN -> "DEVICE_SHUTDOWN"
            Event.DEVICE_STARTUP -> "DEVICE_STARTUP"
            Event.CONFIGURATION_CHANGE -> "CONFIGURATION_CHANGE"
            Event.FOREGROUND_SERVICE_START -> "FOREGROUND_SERVICE_START"
            Event.FOREGROUND_SERVICE_STOP -> "FOREGROUND_SERVICE_STOP"
            Event.KEYGUARD_HIDDEN -> "KEYGUARD_HIDDEN"
            Event.KEYGUARD_SHOWN -> "KEYGUARD_SHOWN"
            Event.SCREEN_INTERACTIVE -> "SCREEN_INTERACTIVE"
            Event.SCREEN_NON_INTERACTIVE -> "SCREEN_NON_INTERACTIVE"
            Event.SHORTCUT_INVOCATION -> "SHORTCUT_INVOCATION"
            Event.STANDBY_BUCKET_CHANGED -> "STANDBY_BUCKET_CHANGED"
            Event.USER_INTERACTION -> "USER_INTERACTION"
            Event.NONE -> "NONE"
            else -> "UNKNOWN"
        }
    }

    fun Long.toZdt(): ZonedDateTime {
        val instant = Instant.ofEpochMilli(this)
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
    }

    @Test
    fun insights() = runTest {
        val healthRepo = Sahha.di.healthConnectRepo
        val insightsRepo = Sahha.di.insightsRepo

        val records = healthRepo.getRecords(
            StepsRecord::class, TimeRangeFilter.between(
                ZonedDateTime.of(
                    LocalDate.now().minusDays(1), LocalTime.of(0, 0), ZoneId.systemDefault()
                ).toLocalDateTime(),
                ZonedDateTime.of(
                    LocalDate.now(), LocalTime.of(0, 0), ZoneId.systemDefault()
                ).toLocalDateTime(),
            )
        )

        Assert.assertNotNull(records)
        if (records == null) return@runTest

        val aggregates = healthRepo.getAggregateRecordsByPeriod(
            setOf(StepsRecord.COUNT_TOTAL),
            TimeRangeFilter.between(
                ZonedDateTime.of(
                    LocalDate.now().minusDays(1), LocalTime.of(0, 0), ZoneId.systemDefault()
                ).toLocalDateTime(),
                ZonedDateTime.of(
                    LocalDate.now(), LocalTime.of(0, 0), ZoneId.systemDefault()
                ).toLocalDateTime()
            ), Period.ofDays(1)
        )
        Assert.assertNotNull(aggregates)
        if (aggregates == null) return@runTest

        Assert.assertEquals(aggregates.count(), 1)


        val countLong = aggregates.first().result.get(StepsRecord.COUNT_TOTAL)

        countLong?.also {
            val countHealth = it.toDouble()
            val countSdk = insightsRepo.getStepCount(records)
            Assert.assertEquals(countHealth, countSdk, 0.5)
        } ?: Assert.fail("Health Connect total step count was null!")
    }

    @Test
    fun insightsWeek() = runTest {
        val subtractDays = 2L
        val insightsRepo = Sahha.di.insightsRepo
        val healthRepo = Sahha.di.healthConnectRepo
        val zone = ZonedDateTime.now().zone
        val aggregates = healthRepo.getAggregateRecordsByPeriod(
            setOf(StepsRecord.COUNT_TOTAL),
            TimeRangeFilter.between(
                ZonedDateTime.of(
                    LocalDate.now().minusDays(subtractDays), LocalTime.of(0, 0), zone
                ).toLocalDateTime(),
                ZonedDateTime.of(
                    LocalDate.now(), LocalTime.of(23, 59, 59, 99), zone
                ).toLocalDateTime()
            ), Period.ofDays(1)
        )
        val records = healthRepo.getRecords(
            StepsRecord::class,
            TimeRangeFilter.between(
                ZonedDateTime.of(
                    LocalDate.now().minusDays(subtractDays), LocalTime.of(0, 0), zone
                ).toLocalDateTime(),
                ZonedDateTime.of(
                    LocalDate.now(), LocalTime.of(23, 59, 59, 99), zone
                ).toLocalDateTime()
            )
        )
        records?.also { day ->
            val packageToSteps = mutableMapOf<String, Long>()

            println("************ Records Raw ****************")
            for (record in day) {
                val start = ZonedDateTime.ofInstant(record.startTime, record.startZoneOffset)
                val count = record.count
                val origin = record.metadata.dataOrigin.packageName
                println("$start\t$count\t$origin")
                packageToSteps[origin] = (packageToSteps[origin] ?: 0) + count
            }
            println("********************************\n\n")
            println("************ Records Summed ****************")
            packageToSteps.forEach {
                println("${it.key}\t\t${it.value}")
            }
            println("**********************************\n\n")
            println()

            println("************ Records SDK **********")
            val stepsSdk = insightsRepo.getStepCount(day)
            println(stepsSdk)
            println("************************************\n\n")
            println()
        }

        aggregates?.also { aggs ->
            println("************** Aggregates ********************")
            for (agg in aggs) {
                val count = agg.result.get(StepsRecord.COUNT_TOTAL)
                if (agg.result.dataOrigins.isNotEmpty()) {
                    println("Origins:")
                    for (origin in agg.result.dataOrigins) {
                        println(agg.result.dataOrigins)
                    }
                }
                println()
                println("${ZonedDateTime.of(agg.startTime, zone)}\t$count")
            }
            println("**********************************")
            println()
        }
    }

    @Test
    fun cultureConversion() = runTest {
        println("*********************************")
        calendar = android.icu.util.Calendar
            .getInstance(
                ULocale("@calendar=buddhist")
            ).apply {
                isLenient = true
            }
        println(calendar?.type)
        year = calendar?.get(android.icu.util.Calendar.YEAR) ?: -1
        month = calendar?.get(android.icu.util.Calendar.MONTH)?.plus(1) ?: -1
        day = calendar?.get(android.icu.util.Calendar.DAY_OF_MONTH) ?: -1
        hour = calendar?.get(android.icu.util.Calendar.HOUR_OF_DAY) ?: -1
        minute = calendar?.get(android.icu.util.Calendar.MINUTE) ?: -1
        println(
            "${String.format("%02d", year)}-${
                String.format(
                    "%02d",
                    month
                )
            }-${String.format("%02d", day)}, ${String.format("%02d", hour)}${
                String.format(
                    "%02d",
                    minute
                )
            } hours"
        )
        println(
            tm.epochMillisToISO(calendar?.timeInMillis ?: throw NullPointerException())
        )
        println("*********************************")
        calendar = android.icu.util.Calendar
            .getInstance(
                ULocale("@calendar=japanese")
            ).apply {
                isLenient = true
            }
        println(calendar?.type)
        year = calendar?.get(android.icu.util.Calendar.YEAR) ?: -1
        month = calendar?.get(android.icu.util.Calendar.MONTH)?.plus(1) ?: -1
        day = calendar?.get(android.icu.util.Calendar.DAY_OF_MONTH) ?: -1
        hour = calendar?.get(android.icu.util.Calendar.HOUR_OF_DAY) ?: -1
        minute = calendar?.get(android.icu.util.Calendar.MINUTE) ?: -1
        println(
            "${String.format("%02d", year)}-${
                String.format(
                    "%02d",
                    month
                )
            }-${String.format("%02d", day)}, ${String.format("%02d", hour)}${
                String.format(
                    "%02d",
                    minute
                )
            } hours"
        )
        println(
            tm.dateToISO(calendar?.time ?: throw NullPointerException())
        )
        println("*********************************")
        calendar = android.icu.util.Calendar
            .getInstance(
                ULocale("@calendar=gregory")
            ).apply {
                isLenient = true
            }
        println(calendar?.type)
        year = calendar?.get(android.icu.util.Calendar.YEAR) ?: -1
        month = calendar?.get(android.icu.util.Calendar.MONTH)?.plus(1) ?: -1
        day = calendar?.get(android.icu.util.Calendar.DAY_OF_MONTH) ?: -1
        hour = calendar?.get(android.icu.util.Calendar.HOUR_OF_DAY) ?: -1
        minute = calendar?.get(android.icu.util.Calendar.MINUTE) ?: -1
        println(
            "${String.format("%02d", year)}-${
                String.format(
                    "%02d",
                    month
                )
            }-${String.format("%02d", day)}, ${String.format("%02d", hour)}${
                String.format(
                    "%02d",
                    minute
                )
            } hours"
        )
        println(
            tm.epochMillisToISO(calendar?.timeInMillis ?: throw NullPointerException())
        )
        println("*********************************")

//            println("type: $type")
//        println("calendar time: ${calendar?.time}")
//        println("instant: $instant")
//        println("iso time: $time")
//        println("*********************************")

//        ZoneOffset.getAvailableZoneIds().forEach { id ->
//            val zdt = tm.ISOToZonedDateTime(time)
//            val time = tm.localDateTimeToISO(zdt.toLocalDateTime(), ZoneId.of(id))
//            list[id] = (time)
////            println(SahhaConverterUtility.convertToJsonString(cal))
//        }
//
//
//        val sorted = list.toList().sortedBy { (_, value) -> value }.toMap()
//        sorted.forEach {
//            println("${it.value}\t${it.key}")
//        }
//        println("*********************************")

//        val list2 = mutableListOf<String>()
//        list.forEach {
//            val tm = SahhaTimeManager()
//            val time = tm.ISOToDate(it)
//            list2.add(time.toString())
//            list2.sortBy { it }
//        }
//        list2.forEach { println(it) }
    }

    @Test
    fun killswitch() = runTest {
        suspendCoroutine { cont ->
            runBlocking {
                delay(3000)
                val repo = SensorRepoImpl(
                    activity,
                    CoroutineScope(Dispatchers.Default),
                    CoroutineScope(Dispatchers.IO),
                    Sahha.di.deviceUsageRepo,
                    Sahha.di.sleepDao,
                    Sahha.di.movementDao,
                    Sahha.di.authRepo,
                    Sahha.di.sahhaConfigRepo,
                    Sahha.di.sahhaErrorLogger,
                    Sahha.di.mutex,
                    Sahha.di.api,
                    Sahha.di.postChunkManager,
                    Sahha.di.permissionManager,
                    Sahha.di.timeManager
                )

                repo.handleResponse(
                    response = Response.error(410, "{}".toResponseBody()),
                    { Response.error(410, "{}".toResponseBody()) },
                    { error, successful ->
                        println(error)
                        cont.resume(Unit)
                    }
                )
            }
        }
    }

    @Test
    fun exercise_dateTime() = runTest {
        val sessions = getExerciseSessions()
        println(logJsonString("", sessions))

//        val newSessions = getExerciseSessions()
//        val json = logJsonString("", sessions)
//        println(json)
//
//        val jsonLogs =
//            logJsonString("", sessions.map { ex -> ex.segments.map { it.toSahhaDataLogDto(ex) } })
//        println(jsonLogs)
//
//        val jsonLogsLap =
//            logJsonString("", sessions.map { ex -> ex.laps.map { it.toSahhaDataLogDto(ex) } })
//        println(jsonLogsLap)
    }

//    @Test
//    fun stepsAggregate() = runTest {
//        suspendCoroutine { cont ->
//            Sahha.enableSensors(ApplicationProvider.getApplicationContext()) { error, status ->
//                runBlocking {
//                    val now = LocalDateTime.now()
//                    val response1 =
//                        Sahha.di.sahhaInteractionManager.insights.checkAndAddStepsInsightPeriod(
//                            LocalDateTime.of(
//                                now.minusDays(7).toLocalDate(),
//                                LocalTime.of(Constants.ALARM_12AM, 0)
//                            ),
//                            LocalDateTime.of(
//                                now.toLocalDate(),
//                                LocalTime.of(Constants.ALARM_12AM, 0)
//                            )
//                        )
//                    Sahha.di.sahhaInteractionManager.insights.checkAndAddStepsInsight(
//                        LocalDateTime.of(
//                            now.minusDays(2).toLocalDate(),
//                            LocalTime.of(Constants.ALARM_12AM, 0)
//                        ),
//                        LocalDateTime.of(
//                            now.toLocalDate().minusDays(1),
//                            LocalTime.of(Constants.ALARM_12AM, 0)
//                        )
//                    )
//                    println(SahhaConverterUtility.convertToJsonString(response1))
//                    cont.resume(Unit)
//                }
//            }
//        }
//    }

    private fun getExerciseSessions(): List<ExerciseSessionRecord> {
        val now = ZonedDateTime.now()
        return listOf(
            ExerciseSessionRecord(
                now.minusMinutes(30).toInstant(), now.offset,
                now.toInstant(), now.offset,
                ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS,
                segments = listOf<ExerciseSegment>(
                    ExerciseSegment(
                        now.minusMinutes(30).toInstant(),
                        now.minusMinutes(29).toInstant(),
                        ExerciseSegment.EXERCISE_SEGMENT_TYPE_PULL_UP,
                        10
                    ),
                    ExerciseSegment(
                        now.minusMinutes(28).toInstant(),
                        now.minusMinutes(27).toInstant(),
                        ExerciseSegment.EXERCISE_SEGMENT_TYPE_PULL_UP,
                        10
                    ),
                )
            ),
            ExerciseSessionRecord(
                now.minusMinutes(15).toInstant(), now.offset,
                now.toInstant(), now.offset,
                ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS,
                segments = listOf<ExerciseSegment>(
                    ExerciseSegment(
                        now.minusMinutes(15).toInstant(),
                        now.minusMinutes(14).toInstant(),
                        ExerciseSegment.EXERCISE_SEGMENT_TYPE_CRUNCH,
                        10
                    ),
                    ExerciseSegment(
                        now.minusMinutes(13).toInstant(),
                        now.minusMinutes(12).toInstant(),
                        ExerciseSegment.EXERCISE_SEGMENT_TYPE_CRUNCH,
                        10
                    ),
                )
            ),
            ExerciseSessionRecord(
                now.minusMinutes(15).toInstant(), now.offset,
                now.toInstant(), now.offset,
                ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
                laps = listOf(
                    ExerciseLap(
                        startTime = now.minusSeconds(45).toInstant(),
                        endTime = now.toInstant(),
                        length = Length.meters(100.0)
                    )
                )
            )
        )
    }

    @Test
    fun nowISO() = runTest {
        val tm = SahhaTimeManager()
        println(tm.nowInISO())
    }

    @Test
    fun bloodPressureError() = runTest {
        val now = ZonedDateTime.now()
        suspendCoroutine { cont ->
            backgroundScope.launch {
                Sahha.di.healthConnectRepo.postBloodPressureData(
                    listOf(
                        BloodPressureRecord(
                            now.toInstant(),
                            now.offset,
                            Pressure.millimetersOfMercury(100.0),
                            Pressure.millimetersOfMercury(100.0),
                            BloodPressureRecord.BODY_POSITION_LYING_DOWN,
                            BloodPressureRecord.MEASUREMENT_LOCATION_LEFT_UPPER_ARM,
                        )
                    )
                ) { error, successful ->
                    println(error)
                    Assert.assertEquals(true, successful)
                    cont.resume(Unit)
                }
            }
        }
    }

    @Test
    fun errorLogging() = runTest {
        suspendCoroutine<Unit> { cont ->
            backgroundScope.launch {
                Sahha.di.sensorRepo.postPhoneScreenLockData(
                    listOf(
                        PhoneUsage(
                            true, true, "some_invalid_time_string"
                        )
                    ),
                ) { error, successful ->
                    println(error)
                    Assert.assertEquals(false, successful)
                    cont.resume(Unit)
                }
            }
        }
    }

    @Test
    fun custom_shouldShowRationale() = runTest {
        val sharedPrefs = activity.getSharedPreferences("test", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().commit()
        Assert.assertEquals(SahhaSensorStatus.pending, getStatus())
        Assert.assertEquals(SahhaSensorStatus.disabled, getStatus())
    }

    private fun getStatus(): SahhaSensorStatus {
        val sharedPrefs = activity.getSharedPreferences("test", Context.MODE_PRIVATE)
//        if (!sharedPrefs.contains("rationale")) sharedPrefs.edit().putBoolean("rationale", true)
//            .commit()

        return if (sharedPrefs.getBoolean("rationale", true)) {
            sharedPrefs.edit().putBoolean("rationale", false).commit()
            SahhaSensorStatus.pending
        } else SahhaSensorStatus.disabled
    }

    @Test
    fun realExercise() = runTest {
        val repo = Sahha.di.healthConnectRepo

        val records = repo.getNewRecords(ExerciseSessionRecord::class)
        val json = logJsonString("", records)
        writeToFile("ExerciseSessionRecordRealWorld", json)
    }

    @Test
    fun jsonStringExercise() = runTest {
        val meta = Metadata(
            id = "test-meta-id",
            dataOrigin = DataOrigin(activity.packageName),
            lastModifiedTime = ZonedDateTime.now().toInstant(),
            device = Device(
                manufacturer = Build.MANUFACTURER,
                model = Build.MODEL,
                type = Device.TYPE_PHONE
            ),
            recordingMethod = Metadata.RECORDING_METHOD_MANUAL_ENTRY
        )

        val exerciseSessions = ExerciseSessionRecord(
            ZonedDateTime.now().minusMinutes(1).toInstant(),
            ZonedDateTime.now().offset,
            ZonedDateTime.now().plusMinutes(1).toInstant(),
            ZonedDateTime.now().offset,
            ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
            laps = listOf(
                ExerciseLap(
                    ZonedDateTime.now().toInstant(),
                    ZonedDateTime.now().toInstant(),
                    Length.meters(100.0)
                ),
                ExerciseLap(
                    ZonedDateTime.now().toInstant(),
                    ZonedDateTime.now().toInstant(),
                    Length.meters(100.0)
                ),
                ExerciseLap(
                    ZonedDateTime.now().toInstant(),
                    ZonedDateTime.now().toInstant(),
                    Length.meters(100.0)
                )
            ),
            exerciseRoute = ExerciseRoute(
                listOf(
                    ExerciseRoute.Location(
                        ZonedDateTime.now().toInstant(),
                        0.0,
                        0.0,
                        Length.meters(0.5),
                        Length.meters(0.5),
                        Length.meters(0.5)
                    ),
                    ExerciseRoute.Location(
                        ZonedDateTime.now().toInstant(),
                        0.0,
                        5.0,
                        Length.meters(0.5),
                        Length.meters(0.5),
                        Length.meters(0.5)
                    ),
                    ExerciseRoute.Location(
                        ZonedDateTime.now().toInstant(),
                        5.0,
                        5.0,
                        Length.meters(0.5),
                        Length.meters(0.5),
                        Length.meters(0.5)
                    ),
                    ExerciseRoute.Location(
                        ZonedDateTime.now().toInstant(),
                        5.0,
                        0.0,
                        Length.meters(0.5),
                        Length.meters(0.5),
                        Length.meters(0.5)
                    )
                )
            ),
            metadata = meta
        )

        val json = logJsonString("", exerciseSessions)
        writeToFile("ExerciseSessionRecord_Running", json)
    }

    @Test
    fun jsonStrings() = runTest {
        val meta = Metadata(
            id = "test-meta-id",
            dataOrigin = DataOrigin(activity.packageName),
            lastModifiedTime = ZonedDateTime.now().toInstant(),
            device = Device(
                manufacturer = Build.MANUFACTURER,
                model = Build.MODEL,
                type = Device.TYPE_PHONE
            ),
            recordingMethod = Metadata.RECORDING_METHOD_MANUAL_ENTRY
        )

        val activeCalsBurned = ActiveCaloriesBurnedRecord(
            ZonedDateTime.now().toInstant(),
            ZonedDateTime.now().offset,
            ZonedDateTime.now().toInstant(),
            ZonedDateTime.now().offset,
            Energy.calories(100.0),
            metadata = meta
        )

        var j = logJsonString("", activeCalsBurned)
        writeToFile("ActiveCaloriesBurnedRecord", j)

        val bodyTemp = BodyTemperatureRecord(
            ZonedDateTime.now().toInstant(),
            ZonedDateTime.now().offset,
            Temperature.celsius(100.0),
            BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_ARMPIT,
            metadata = meta
        )
        j = logJsonString("", bodyTemp)
        writeToFile("BodyTemperatureRecord", j)

        val bodyTempMeasurementLocations = object {
            val ARMPIT = "armpit"
            val FINGER = "finger"
            val FOREHEAD = "forehead"
            val MOUTH = "mouth"
            val RECTUM = "rectum"
            val TEMPORAL_ARTERY = "temporal_artery"
            val TOE = "toe"
            val EAR = "ear"
            val WRIST = "wrist"
            val VAGINA = "vagina"
        }
        j = logJsonString("", bodyTempMeasurementLocations)
        writeToFile("BodyTemperatureMeasurementLocations", j)

        val floorsClimbed = FloorsClimbedRecord(
            ZonedDateTime.now().toInstant(),
            ZonedDateTime.now().offset,
            ZonedDateTime.now().toInstant(),
            ZonedDateTime.now().offset,
            100.0,
            metadata = meta
        )
        j = logJsonString("", floorsClimbed)
        writeToFile("FloorsClimbedRecord", j)

        val oxygen = OxygenSaturationRecord(
            ZonedDateTime.now().toInstant(),
            ZonedDateTime.now().offset,
            Percentage(100.0),
            metadata = meta
        )
        j = logJsonString("", oxygen)
        writeToFile("OxygenSaturationRecord", j)

        val totalCals = TotalCaloriesBurnedRecord(
            ZonedDateTime.now().toInstant(),
            ZonedDateTime.now().offset,
            ZonedDateTime.now().toInstant(),
            ZonedDateTime.now().offset,
            Energy.calories(100.0),
            metadata = meta
        )
        j = logJsonString("", totalCals)
        writeToFile("TotalCaloriesBurnedRecord", j)

        val vo2 = Vo2MaxRecord(
            ZonedDateTime.now().toInstant(),
            ZonedDateTime.now().offset,
            100.0,
            Vo2MaxRecord.MEASUREMENT_METHOD_COOPER_TEST,
            metadata = meta
        )
        j = logJsonString("", vo2)
        writeToFile("Vo2MaxRecord", j)

        val vo2MeasurementMethods = object {
            val METABOLIC_CART = "metabolic_cart"
            val HEART_RATE_RATIO = "heart_rate_ratio"
            val COOPER_TEST = "cooper_test"
            val MULTISTAGE_FITNESS_TEST = "multistage_fitness_test"
            val ROCKPORT_FITNESS_TEST = "rockport_fitness_test"
            val OTHER = "other"
        }

        j = logJsonString("", vo2MeasurementMethods)
        writeToFile("Vo2MaxMeasurementMethods", j)
    }

    private fun writeToFile(fileName: String, jsonString: String, context: Context = activity) {
        val name = "$fileName.json"
        context.openFileOutput(name, Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        }
    }


    @Test
    fun test() = runTest {
        val data = Sahha.di.healthConnectRepo.getAggregateRecordsByDuration(
            setOf(
                StepsRecord.COUNT_TOTAL
            ),
            TimeRangeFilter.Companion.between(
                LocalDateTime.of(2023, 9, 18, 0, 0, 0),
                LocalDateTime.of(2023, 9, 19, 0, 0, 0)
            ),
            Duration.ofMinutes(15)
        )
        logJsonString("hourly/24h", data)
    }

    @Test
    fun test_period() = runTest {
        val data = Sahha.di.healthConnectRepo.getAggregateRecordsByPeriod(
            setOf(
                StepsRecord.COUNT_TOTAL
            ),
            TimeRangeFilter.Companion.between(
                LocalDateTime.of(2023, 9, 12, 0, 0, 0),
                LocalDateTime.of(2023, 9, 19, 0, 0, 0)
            ),
            Period.ofDays(1)
        )
        logJsonString("daily/week", data)
    }

    private suspend fun insertMetadataRecord() {
        Sahha.di.healthConnectClient?.insertRecords(
            listOf(
                StepsRecord(
                    startTime = Instant.now().minusSeconds(60),
                    endTime = Instant.now(),
                    startZoneOffset = null,
                    endZoneOffset = null,
                    count = 100,
                    metadata = Metadata(
                        id = "test-id",
                        dataOrigin = DataOrigin("test.package.name"),
                        lastModifiedTime = Instant.now(),
                        clientRecordId = "test-client-record-id",
                        clientRecordVersion = 1,
                        device = Device(
                            manufacturer = "minTheDev",
                            model = "Instance2000",
                            type = Device.TYPE_PHONE
                        ),
                        recordingMethod = 0
                    )
                )
            )
        )
    }

    @Test
    fun test_raw() = runTest {
        insertMetadataRecord()
        val data = Sahha.di.healthConnectRepo.getRecords(
            SpeedRecord::class,
            TimeRangeFilter.Companion.between(
                LocalDateTime.of(
                    2023, 9, 18, 0, 0, 0
                ),
                LocalDateTime.of(
                    2023, 9, 25, 0, 0, 0
                )
            )
        )
        logJsonString("raw", data)
    }

    @Test
    fun test_enum() {
        val mapper = Sahha.di.healthConnectConstantsMapper
        val displayString = mapper.devices(Device.TYPE_SMART_DISPLAY)
        Assert.assertEquals("SMART_DISPLAY", displayString)

        val active =
            mapper.recordingMethod(android.health.connect.datatypes.Metadata.RECORDING_METHOD_ACTIVELY_RECORDED)
        Assert.assertEquals("RECORDING_METHOD_ACTIVELY_RECORDED", active)
    }

    @Test
    fun test_insert() = runTest {
        Sahha.di.healthConnectClient?.also {
            it.insertRecords(
                listOf(
                    StepsRecord(
                        startTime = Instant.now(),
                        endTime = Instant.now(),
                        count = 100,
                        startZoneOffset = null,
                        endZoneOffset = null,
                        metadata = Metadata(
                            dataOrigin = DataOrigin("fake package name"),
                            device = Device(
                                type = Device.TYPE_PHONE,
                                manufacturer = "Fake",
                                model = "Fake"
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun test_before_after_queries() = runTest {
        val repo = Sahha.di.healthConnectRepo
        repo.clearAllQueries()

        val records = repo.getNewRecords(HeartRateRecord::class)
        println(records?.size?.toByte())
        Assert.assertEquals(false, records.isNullOrEmpty())

        repo.saveLastSuccessfulQuery(
            HeartRateRecord::class,
            ZonedDateTime.now()
        )
        val last = repo.getLastSuccessfulQuery(HeartRateRecord::class)
        Assert.assertEquals(false, last == null)

        val client = Sahha.di.healthConnectClient
        Assert.assertEquals(false, client == null)

        val nowish = ZonedDateTime.now()
        val offset = nowish.offset
        client?.also {
            it.insertRecords(
                listOf(
                    HeartRateRecord(
                        startTime = nowish.toInstant(),
                        startZoneOffset = offset,
                        endTime = nowish.plusSeconds(600).toInstant(),
                        endZoneOffset = offset,
                        samples = listOf(
                            HeartRateRecord.Sample(
                                beatsPerMinute = 100,
                                time = nowish.plusSeconds(10).toInstant(),
                            ),
                            HeartRateRecord.Sample(
                                beatsPerMinute = 100,
                                time = nowish.plusSeconds(20).toInstant(),
                            ),
                            HeartRateRecord.Sample(
                                beatsPerMinute = 100,
                                time = nowish.plusSeconds(30).toInstant(),
                            )
                        )
                    )
                )
            )

            val nextRecords = repo.getNewRecords(HeartRateRecord::class)
            println(nextRecords)
            Assert.assertEquals(false, nextRecords.isNullOrEmpty())
        }
    }

    @Test
    fun test_precise_reading() = runTest {
        val client = Sahha.di.healthConnectClient
        client ?: return@runTest

        val preciseTime = ZonedDateTime.now()
//            .withNano(0)

        client.insertRecords(
            listOf(
                HeartRateRecord(
                    startTime = preciseTime.toInstant(),
                    startZoneOffset = preciseTime.offset,
                    endTime = preciseTime.plusSeconds(60).toInstant(),
                    endZoneOffset = preciseTime.offset,
                    samples = listOf(
                        HeartRateRecord.Sample(
                            beatsPerMinute = 100,
                            time = preciseTime.plusSeconds(10).toInstant(),
                        ),
                        HeartRateRecord.Sample(
                            beatsPerMinute = 100,
                            time = preciseTime.plusSeconds(20).toInstant(),
                        ),
                        HeartRateRecord.Sample(
                            beatsPerMinute = 100,
                            time = preciseTime.plusSeconds(30).toInstant(),
                        )
                    )
                )
            )
        )

        val preciseTimeRecord = client.readRecords(
            ReadRecordsRequest(
                HeartRateRecord::class,
                TimeRangeFilter.after(
                    preciseTime.toInstant()
                )
            )
        ).records
        val beforePreciseTimeRecord = client.readRecords(
            ReadRecordsRequest(
                HeartRateRecord::class,
                TimeRangeFilter.after(
                    preciseTime.minusSeconds(1).toInstant()
                )
            )
        ).records
        val afterPreciseTimeRecord = client.readRecords(
            ReadRecordsRequest(
                HeartRateRecord::class,
                TimeRangeFilter.after(
                    preciseTime.minusSeconds(1).toInstant()
                )
            )
        ).records

        println("*******************************")
        println("*******************************")
        println("exact: ${SahhaConverterUtility.convertToJsonString(preciseTimeRecord.last())}")
        println("before: ${SahhaConverterUtility.convertToJsonString(beforePreciseTimeRecord.last())}")
        println("after: ${SahhaConverterUtility.convertToJsonString(afterPreciseTimeRecord.last())}")
        println("*******************************")
        println("*******************************")
        Assert.assertNotNull(preciseTimeRecord)
        Assert.assertNotNull(beforePreciseTimeRecord)
        Assert.assertNotNull(afterPreciseTimeRecord)
    }

    @Test
    fun test_epoch_conversion() = runTest {
        val repo = Sahha.di.healthConnectRepo
        val now = ZonedDateTime.now()
        repo.clearAllQueries()

        repo.saveLastSuccessfulQuery(
            HeartRateRecord::class, now
        )

        val saved = repo.getLastSuccessfulQuery(HeartRateRecord::class)
        saved?.also { Assert.assertEquals(now, it) }
    }

    @Test
    fun query_bug() = runTest {
        val client = HealthConnectClient.getOrCreate(activity)
        val inst = Instant.ofEpochMilli(1697138969377)
        val ldt = LocalDateTime.ofInstant(inst, ZonedDateTime.now().offset)
//        val zdt = ZonedDateTime.ofInstant(inst, ZonedDateTime.now().offset)
        val instJson = SahhaConverterUtility.convertToJsonString(ldt)
        println(instJson)
        println(ldt)
//        Assert.assertEquals("", ldt)
        val records = client.readRecords(
            ReadRecordsRequest(
                HeartRateRecord::class,
                TimeRangeFilter.after(inst)
            )
        ).records
        val json = SahhaConverterUtility.convertToJsonString(records = records)
        println(json)
        Assert.assertNotNull(records)
        Assert.assertEquals(false, records.isEmpty())
    }

    @Test
    fun test_timeConversion_isCorrect() = runTest {
        val repo = Sahha.di.healthConnectRepo
        val now = ZonedDateTime.now()

        repo.clearAllQueries()
        repo.saveLastSuccessfulQuery(StepsRecord::class, now)
        val timestamp = repo.getLastSuccessfulQuery(StepsRecord::class)

        val nowEpoch =
            now.toInstant().toEpochMilli()
        val timestampEpoch =
            timestamp?.toInstant()
                ?.toEpochMilli()
        println(nowEpoch)
        println(timestampEpoch)

        Assert.assertEquals(
            nowEpoch,
            timestampEpoch
        )
    }

    @Test
    fun changesTokenBehaviour() = runTest {
        val client = Sahha.di.healthConnectClient ?: return@runTest

        val recordId = UUID.randomUUID().toString()
        val end = ZonedDateTime.now().minusSeconds(1)
        val start = end.minusMinutes(1)
        val sample1 = start.plusSeconds(1)
        val sample2 = start.plusSeconds(2)

        var token = client.getChangesToken(
            ChangesTokenRequest(
                setOf(HeartRateRecord::class)
            )
        )

        client.deleteRecords(
            HeartRateRecord::class,
            TimeRangeFilter.before(
                ZonedDateTime
                    .now()
                    .toLocalDateTime()
            )
        )

        client.insertRecords(
            listOf(
                HeartRateRecord(
                    start.toInstant(), start.offset,
                    end.toInstant(), end.offset,
                    listOf(
                        HeartRateRecord.Sample(
                            sample1.toInstant(),
                            100
                        )
                    ),
                    Metadata(clientRecordId = recordId)
                )
            )
        )

        var response = client.getChanges(token)
        var changes = response.changes
        var changedRecord = changes.last()
        if (changedRecord is UpsertionChange) {
            val heartRecord = changedRecord.record as HeartRateRecord
            heartRecord.samples.forEach {
                println("${it.beatsPerMinute} bpm @ ${it.time}")
            }
            Assert.assertEquals(recordId, heartRecord.metadata.clientRecordId)
            Assert.assertEquals(1, heartRecord.samples.count())
        }

        client.updateRecords(
            listOf(
                HeartRateRecord(
                    start.toInstant(), start.offset,
                    end.toInstant(), end.offset,
                    listOf(
                        HeartRateRecord.Sample(
                            sample1.toInstant(),
                            100
                        ),
                        HeartRateRecord.Sample(
                            sample2.toInstant(),
                            100
                        )
                    ),
                    Metadata(clientRecordId = recordId)
                )
            )
        )

        token = response.nextChangesToken
        response = client.getChanges(token)
        changes = response.changes
        changedRecord = changes.last()
        if (changedRecord is UpsertionChange) {
            val heartRecord = changedRecord.record as HeartRateRecord
            heartRecord.samples.forEach {
                println("${it.beatsPerMinute} bpm @ ${it.time}")
            }
            Assert.assertEquals(recordId, heartRecord.metadata.clientRecordId)
            Assert.assertEquals(2, heartRecord.samples.count())
        }
    }

    @Test
    fun afterQuery_isFoundAtOrBeforeStartTime() = runTest {
        val client = Sahha.di.healthConnectClient ?: return@runTest
        val end = ZonedDateTime.now().minusSeconds(1)
        val start = end.minusMinutes(1)

        client.deleteRecords(
            HeartRateRecord::class,
            TimeRangeFilter
                .before(
                    ZonedDateTime
                        .now()
                        .toLocalDateTime()
                )
        )

        client.insertRecords(
            listOf(
                HeartRateRecord(
                    start.toInstant(), start.offset,
                    end.toInstant(), end.offset,
                    listOf(
                        HeartRateRecord.Sample(
                            start.plusSeconds(1).toInstant(),
                            100
                        )
                    )
                )
            )
        )

        val records_start_before = client.readRecords(
            ReadRecordsRequest(
                HeartRateRecord::class,
                TimeRangeFilter.Companion.after(start.minusSeconds(1).toInstant())
            )
        )
        val records_start_exact = client.readRecords(
            ReadRecordsRequest(
                HeartRateRecord::class,
                TimeRangeFilter.Companion.after(start.toInstant())
            )
        )
        val records_start_after = client.readRecords(
            ReadRecordsRequest(
                HeartRateRecord::class,
                TimeRangeFilter.Companion.after(start.plusSeconds(1).toInstant())
            )
        )

        Assert.assertEquals(false, records_start_before.records.isEmpty())
        Assert.assertEquals(false, records_start_exact.records.isEmpty())
        Assert.assertEquals(true, records_start_after.records.isEmpty())

        val records_end_before = client.readRecords(
            ReadRecordsRequest(
                HeartRateRecord::class,
                TimeRangeFilter.Companion.after(end.minusSeconds(1).toInstant())
            )
        )
        val records_end_exact = client.readRecords(
            ReadRecordsRequest(
                HeartRateRecord::class,
                TimeRangeFilter.Companion.after(end.toInstant())
            )
        )
        val records_end_after = client.readRecords(
            ReadRecordsRequest(
                HeartRateRecord::class,
                TimeRangeFilter.Companion.after(end.plusSeconds(1).toInstant())
            )
        )

        Assert.assertEquals(true, records_end_before.records.isEmpty())
        Assert.assertEquals(true, records_end_exact.records.isEmpty())
        Assert.assertEquals(true, records_end_after.records.isEmpty())
    }

    @Test
    fun shealthMeta() = runTest {
        val repo = Sahha.di.healthConnectRepo

        repo.clearAllQueries()
        val records = repo.getNewRecords(HeartRateRecord::class)
        val shealthRecord = records?.find { it.metadata.dataOrigin.packageName.contains("shealth") }
        val shealthRecordLast =
            records?.findLast { it.metadata.dataOrigin.packageName.contains("shealth") }
        val version = shealthRecord?.metadata?.clientRecordVersion
        val versionLast = shealthRecordLast?.metadata?.clientRecordVersion

        println(version)
        println(SahhaConverterUtility.convertToJsonString(shealthRecord))
        println(versionLast)
        println(SahhaConverterUtility.convertToJsonString(shealthRecordLast))
        Assert.assertEquals(version, versionLast)
    }

    @Test
    fun getNewRecords() = runTest {
        val client = Sahha.di.healthConnectClient ?: return@runTest
        val repo = Sahha.di.healthConnectRepo
        val heart = HeartRateRecord::class
        repo.clearAllQueries()
        client.deleteRecords(heart, TimeRangeFilter.before(ZonedDateTime.now().toInstant()))

        var records = repo.getNewRecords(heart)
        Assert.assertEquals(false, records.isNullOrEmpty())

        val now = ZonedDateTime.now().minusSeconds(1)
        client.insertRecords(
            listOf(
                HeartRateRecord(
                    now.minusMinutes(10).toInstant(), now.offset,
                    now.toInstant(), now.offset,
                    listOf(
                        HeartRateRecord.Sample(
                            now.toInstant(), 100
                        )
                    )
                )
            )
        )

        records = repo.getNewRecords(heart)
        Assert.assertEquals(false, records.isNullOrEmpty())
        repo.saveLastSuccessfulQuery(heart, now.plusSeconds(1))

        records = repo.getNewRecords(heart)
        val json = SahhaConverterUtility.convertToJsonString(records)
        println(json)
        Assert.assertEquals(true, records.isNullOrEmpty())
    }
//
//    @Test
//    fun test_initialQuery_isBefore() = runTest {
//        val repo = Sahha.di.healthConnectRepo
//        repo.clearAllQueries()
//
//        val usecase = QueryHealthConnectRecordsUseCase(repo)
//        val records = usecase(StepsRecord::class)
//        logJsonString("Steps before", records)
//        Assert.assertEquals(true, records!!.count() > 1)
//    }
//
//    @Test
//    fun test_consecutiveQuery_isAfter() = runTest {
//        val repo = Sahha.di.healthConnectRepo
//        repo.getLastSuccessfulQuery(StepsRecord::class)
//            ?: repo.saveLastSuccessfulQuery(StepsRecord::class, LocalDateTime.now())
//
//        val usecase = QueryHealthConnectRecordsUseCase(repo)
//        val records = usecase(StepsRecord::class)
//        logJsonString("Steps after", records)
//        Assert.assertEquals(true, records!!.isEmpty())
//    }

//    @Test
//    fun heart_between() = runTest {
//        var heart = Sahha.di.healthConnectRepo.getRecords(
//            HeartRateRecord::class,
//            TimeRangeFilter.Companion.between(
//                LocalDateTime.of(
//                    2023, 9, 17, 0, 0, 0
//                ),
//                LocalDateTime.of(
//                    2023, 9, 17, 1, 0, 0
//                )
//            )
//        )
//        logJsonString("first hour", heart)
//
//        heart = Sahha.di.healthConnectRepo.getRecords(
//            HeartRateRecord::class,
//            TimeRangeFilter.Companion.between(
//                LocalDateTime.of(
//                    2023, 9, 17, 23, 0, 0
//                ),
//                LocalDateTime.of(
//                    2023, 9, 17, 23, 59, 59
//                )
//            )
//        )
//        logJsonString("last hour", heart)
//
//        heart = Sahha.di.healthConnectRepo.getRecords(
//            HeartRateRecord::class,
//            TimeRangeFilter.Companion.between(
//                LocalDateTime.of(
//                    2023, 9, 17, 0, 0, 0
//                ),
//                LocalDateTime.of(
//                    2023, 9, 17, 23, 59, 59
//                )
//            )
//        )
//        logJsonString("one day", heart)
//    }

    @Test
    fun heart_before() = runTest {
//        var heart = Sahha.di.healthConnectRepo.getRecords(
//            HeartRateRecord::class,
//            TimeRangeFilter.Companion.before(
//                LocalDateTime.of(
//                    2023, 9, 17, 1, 0, 0
//                )
//            )
//        )
//        logJsonString("first hour", heart)

//        val heart = Sahha.di.healthConnectRepo.getRecords(
//            HeartRateRecord::class,
//            TimeRangeFilter.Companion.before(
//                LocalDateTime.of(
//                    2023, 9, 17, 23, 59, 59
//                )
//            )
//        )
//        logJsonString("last hour/one day", heart)
    }

//    @Test
//    fun heart_after() = runTest {
//        var heart = Sahha.di.healthConnectRepo.getRecords(
//            HeartRateRecord::class,
//            TimeRangeFilter.Companion.after(
//                LocalDateTime.of(
//                    2023, 9, 17, 0, 0, 0
//                )
//            )
//        )
//        logJsonString("first hour/one day", heart)
//
//        heart = Sahha.di.healthConnectRepo.getRecords(
//            HeartRateRecord::class,
//            TimeRangeFilter.Companion.after(
//                LocalDateTime.of(
//                    2023, 9, 17, 23, 0, 0
//                )
//            )
//        )
//        logJsonString("last hour", heart)
//    }

//    @Test
//    fun test_StepDto() = runTest {
//        val steps = Sahha.di.healthConnectRepo.getRecords(
//            StepsRecord::class,
//            Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS),
//            Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS).plus(24, ChronoUnit.HOURS)
//        )
//        val stepsDto = steps?.map {
//            Sahha.di.healthConnectRepo.toStepDto(it)
//        }
//        logJsonString("Steps DTO List", stepsDto)
//    }

//    @Test
//    fun steps_testBetween() = runTest {
//        var steps = Sahha.di.healthConnectRepo.getSteps(
//            startday = 17, starthour = 0
//        )
//        logJsonString("query first hour", steps)
//
//        steps = Sahha.di.healthConnectRepo.getSteps(
//            startday = 17, starthour = 23
//        )
//        logJsonString("query last hour", steps)
//
//        steps = Sahha.di.healthConnectRepo.getSteps(
//            startday = 17, starthour = 0, endhour = 23
//        )
//        logJsonString("query one day", steps)
//    }
//
//    @Test
//    fun steps_testBefore() = runTest {
//        var steps = Sahha.di.healthConnectRepo.getSteps(
//            TimeRangeFilter.Companion.before(
//                LocalDateTime.of(
//                    2023, 9, 17, 1, 0, 0
//                )
//            )
//        )
//        logJsonString("query first hour", steps)
//
//        steps = Sahha.di.healthConnectRepo.getSteps(
//            TimeRangeFilter.Companion.before(
//                LocalDateTime.of(
//                    2023, 9, 17, 23, 59, 59
//                )
//            )
//        )
//        logJsonString("query last hour/one day", steps)
//    }
//
//    @Test
//    fun steps_testAfter() = runTest {
//        var steps = Sahha.di.healthConnectRepo.getSteps(
//            TimeRangeFilter.Companion.before(
//                LocalDateTime.of(
//                    2023, 9, 17, 0, 0, 0
//                )
//            )
//        )
//        logJsonString("query first hour/one day", steps)
//
//        steps = Sahha.di.healthConnectRepo.getSteps(
//            TimeRangeFilter.Companion.before(
//                LocalDateTime.of(
//                    2023, 9, 17, 23, 0, 0
//                )
//            )
//        )
//        logJsonString("query last hour", steps)
//    }
//
//    @Test
//    fun steps_metadata() = runTest {
//        val metadata = Sahha.di.healthConnectRepo.getSteps(
//            TimeRangeFilter.Companion.before(
//                LocalDateTime.of(
//                    2023, 9, 17, 0, 0, 0
//                )
//            )
//        )?.first()?.metadata
//        println("Device: " + metadata?.device)
//        logJsonString("metadata", metadata)
//    }

    private fun <T> logJsonString(
        headerText: String,
        data: T?
    ): String {
        val json =
            GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(
                    Instant::class.java,
                    JsonSerializer<Instant> { src, _, _ ->
                        JsonPrimitive(src.toString())
                    }
                )
                .registerTypeAdapter(
                    ZoneOffset::class.java,
                    JsonSerializer<ZoneOffset> { src, _, _ ->
                        JsonPrimitive(src.toString())
                    }
                )
                .setDateFormat(DateFormat.TIMEZONE_ISO_FIELD)
                .create()
                .toJson(data)
        println("*******************************")
        println(headerText)
        println("*******************************")
        println(json)

        return json
    }

    private fun writeToFile(json: String) {
        activity.application.openFileOutput("json-output", Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
        val file = File(activity.application.filesDir, "json-output.txt")
    }
}