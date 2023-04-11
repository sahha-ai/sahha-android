package sdk.sahha.android.source

import android.app.Application
import android.content.Context
import androidx.annotation.Keep
import kotlinx.coroutines.runBlocking
import sdk.sahha.android.di.AppComponent
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.di.DaggerAppComponent
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.interaction.SahhaInteractionManager
import java.time.LocalDateTime
import java.util.*

private val tag = "Sahha"

@Keep
object Sahha {
    internal lateinit var sim: SahhaInteractionManager
    internal lateinit var di: AppComponent
    internal lateinit var config: SahhaConfiguration
    internal val notificationManager by lazy { di.notificationManager }

    fun diInitialized(): Boolean {
        return ::di.isInitialized
    }

    fun simInitialized(): Boolean {
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

        runBlocking {
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


    fun analyze(
        includeSourceData: Boolean = false,
        callback: ((error: String?, success: String?) -> Unit)?
    ) {
        sim.userData.analyze(includeSourceData, callback)
    }


    @JvmName("analyzeDate")
    fun analyze(
        includeSourceData: Boolean = false,
        dates: Pair<Date, Date>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        sim.userData.analyze(includeSourceData, dates, callback)
    }

    @JvmName("analyzeLocalDateTime")
    fun analyze(
        includeSourceData: Boolean = false,
        dates: Pair<LocalDateTime, LocalDateTime>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        sim.userData.analyze(includeSourceData, dates, callback)
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


}