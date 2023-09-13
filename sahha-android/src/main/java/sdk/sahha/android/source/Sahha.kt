package sdk.sahha.android.source

import android.app.Application
import android.content.Context
import androidx.annotation.Keep
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sdk.sahha.android.di.AppComponent
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.di.DaggerAppComponent
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.interaction.SahhaInteractionManager
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Keep
object Sahha {
    internal lateinit var sim: SahhaInteractionManager
    internal lateinit var di: AppComponent
    internal lateinit var config: SahhaConfiguration
    internal val notificationManager by lazy { di.notificationManager }

    val isAuthenticated: Boolean
        get() = if (simInitialized()) sim.auth.checkIsAuthenticated() else false

    internal fun diInitialized(): Boolean {
        return ::di.isInitialized
    }

    internal fun simInitialized(): Boolean {
        return ::sim.isInitialized
    }

    fun configure(
        application: Application,
        sahhaSettings: SahhaSettings,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        if (!diInitialized())
            di = DaggerAppComponent.builder()
                .appModule(AppModule(sahhaSettings.environment))
                .context(application)
                .build()

        if (!simInitialized()) sim = di.sahhaInteractionManager

        di.defaultScope.launch {
            sim.configure(application, sahhaSettings, callback)
        }
    }

    fun authenticate(
        appId: String,
        appSecret: String,
        externalId: String,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        sim.auth.authenticate(appId, appSecret, externalId, callback)
    }

    fun authenticate(
        profileToken: String,
        refreshToken: String,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        sim.auth.authenticate(profileToken, refreshToken, callback)
    }

    fun deauthenticate(
        callback: (suspend (error: String?, success: Boolean) -> Unit)
    ) {
        di.ioScope.launch {
            sim.auth.deauthenticate(callback)
        }
    }


    fun analyze(
        callback: ((error: String?, success: String?) -> Unit)?
    ) {
        sim.userData.analyze(callback)
    }


    @JvmName("analyzeDate")
    fun analyze(
        dates: Pair<Date, Date>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        sim.userData.analyze(dates, callback)
    }

    @JvmName("analyzeLocalDateTime")
    fun analyze(
        dates: Pair<LocalDateTime, LocalDateTime>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        sim.userData.analyze(dates, callback)
    }

    fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?) {
        sim.userData.getDemographic(callback)
    }

    fun postDemographic(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        sim.userData.postDemographic(sahhaDemographic, callback)
    }

    fun postSensorData(
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        sim.sensor.postSensorData(callback)
    }

    internal fun getSensorData(
        sensor: SahhaSensor,
        callback: ((error: String?, success: String?) -> Unit)
    ) {
        sim.sensor.getSensorData(sensor, callback)
    }

    fun openAppSettings(context: Context) {
        sim.permission.openAppSettings(context)
    }

    fun enableSensors(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        sim.permission.enableSensors(context, callback)
    }

    fun getSensorStatus(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        sim.permission.getSensorStatus(context, callback)
    }

    fun postError(
        framework: SahhaFramework,
        message: String,
        path: String,
        method: String,
        body: String? = null,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        sim.postAppError(framework, message, path, method, body, callback)
    }

    //TODO Test, delete after
    fun ableToReadSteps(): List<String> {
        val steps = di.healthConnectRepo.getSteps()
        val stepsString = mutableListOf<String>()
        steps?.map {
            stepsString.add(
                "count: ${it.count}\n" +
                        "start: ${it.startTime}\n" +
                        "end: ${it.endTime}"
            )
        }
        return stepsString
    }

    fun getAggregateSteps(
        callback: ((error: String?, steps: List<String>?) -> Unit)
    ) {
        di.ioScope.launch {
            val mostRecentHour = Instant.now().truncatedTo(ChronoUnit.HOURS)
            val steps = di.healthConnectRepo.getHourlySteps(
                start = mostRecentHour.minus(7, ChronoUnit.DAYS),
                end = mostRecentHour
            )?.map {
                var string = ""
                string += "${it.result[StepsRecord.COUNT_TOTAL]}\n"
                string += "${di.timeManager.instantToIsoTime(it.startTime)}\n"
                string += "${di.timeManager.instantToIsoTime(it.endTime)}\n\n"
                return@map string
            }

            callback(null, steps)
        }
    }

    fun getAggregateSleepSessions(
        callback: ((error: String?, sleepSessions: List<String>?) -> Unit)
    ) {
        di.ioScope.launch {
            val mostRecentHour = Instant.now().truncatedTo(ChronoUnit.HOURS)
            val steps = di.healthConnectRepo.getHourlySleepSessions(
                start = mostRecentHour.minus(7, ChronoUnit.DAYS),
                end = mostRecentHour
            )?.map {
                var string = ""
                string += "${it.result[SleepSessionRecord.SLEEP_DURATION_TOTAL]}\n"
                string += "${di.timeManager.instantToIsoTime(it.startTime)}\n"
                string += "${di.timeManager.instantToIsoTime(it.endTime)}\n\n"
                return@map string
            }

            callback(null, steps)
        }
    }
}