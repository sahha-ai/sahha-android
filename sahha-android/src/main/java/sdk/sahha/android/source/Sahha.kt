package sdk.sahha.android.source

import android.app.Application
import android.content.Context
import android.health.connect.datatypes.AggregationType
import android.icu.text.DateFormat
import androidx.annotation.Keep
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.platform.client.proto.RequestProto.AggregateMetricSpec
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.TypeAdapter
import kotlinx.coroutines.launch
import sdk.sahha.android.di.AppComponent
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.di.DaggerAppComponent
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.interaction.SahhaInteractionManager
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
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

    // TODO ****************************
    // TODO Test methods, delete after
    // TODO ****************************
    private fun <T> convertToJsonString(records: List<T>?): String {
        return GsonBuilder()
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
            .toJson(records)
    }

    suspend fun ableToReadSteps(): String {
        val steps = di.healthConnectRepo.getRecords(
            start = Instant.now().minus(1, ChronoUnit.DAYS),
            end = Instant.now(),
            recordType = StepsRecord::class
        )
        return convertToJsonString(steps)
    }

    fun getAggregateSteps(
        callback: ((error: String?, steps: String?) -> Unit)
    ) {
        di.ioScope.launch {
            val mostRecentHour = Instant.now().truncatedTo(ChronoUnit.HOURS)
            val steps = di.healthConnectRepo.getHourlyRecords(
                start = mostRecentHour.minus(1, ChronoUnit.DAYS),
                end = mostRecentHour,
                metrics = setOf(StepsRecord.COUNT_TOTAL)
            )

            callback(null, convertToJsonString(steps))
        }
    }

    fun getAggregateSleepSessions(
        callback: ((error: String?, sleepSessions: String?) -> Unit)
    ) {
        di.ioScope.launch {
            val mostRecentHour = Instant.now().truncatedTo(ChronoUnit.HOURS)
            val steps = di.healthConnectRepo.getHourlyRecords(
                start = mostRecentHour.minus(7, ChronoUnit.DAYS),
                end = mostRecentHour,
                metrics = setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL)
            )

            callback(null, convertToJsonString(steps))
        }
    }
}