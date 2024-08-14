package sdk.sahha.android.source

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.Session
import sdk.sahha.android.di.AppComponent
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.di.DaggerAppComponent
import sdk.sahha.android.domain.interaction.SahhaInteractionManager
import java.time.LocalDateTime
import java.util.Date

private const val tag = "Sahha"

@Keep
object Sahha {
    internal lateinit var sim: SahhaInteractionManager
    internal lateinit var di: AppComponent
    internal val notificationManager by lazy { di.sahhaNotificationManager }

    val isAuthenticated: Boolean
        get() = if (simInitialized()) sim.auth.checkIsAuthenticated() else false

    val profileToken: String?
        get() = di.authRepo.getToken()

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
        saveEnvironment(application, sahhaSettings.environment.ordinal)

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

    private fun saveEnvironment(context: Context, envInt: Int) {
        val prefs = context.getSharedPreferences(Constants.CONFIGURATION_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(Constants.ENVIRONMENT_KEY, envInt).apply()
    }

    fun authenticate(
        appId: String,
        appSecret: String,
        externalId: String,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        if (!sahhaIsConfigured()) {
            callback(SahhaErrors.sahhaNotConfigured, false)
            return
        }

        sim.auth.authenticate(appId, appSecret, externalId, callback)
    }

    fun authenticate(
        profileToken: String,
        refreshToken: String,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        if (!sahhaIsConfigured()) {
            callback(SahhaErrors.sahhaNotConfigured, false)
            return
        }

        sim.auth.authenticate(profileToken, refreshToken, callback)
    }

    fun deauthenticate(
        callback: (suspend (error: String?, success: Boolean) -> Unit)
    ) {
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            if (!sahhaIsConfigured()) {
                callback(SahhaErrors.sahhaNotConfigured, false)
                return@launch
            }

            sim.auth.deauthenticate(callback)
        }
    }


    fun analyze(
        callback: ((error: String?, success: String?) -> Unit)?
    ) {
        if (!sahhaIsConfigured()) {
            callback?.invoke(SahhaErrors.sahhaNotConfigured, null)
            return
        }

        sim.userData.analyze(callback)
    }


    @JvmName("analyzeDate")
    fun analyze(
        dates: Pair<Date, Date>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        if (!sahhaIsConfigured()) {
            callback?.invoke(SahhaErrors.sahhaNotConfigured, null)
            return
        }

        sim.userData.analyze(dates, callback)
    }

    @JvmName("analyzeLocalDateTime")
    fun analyze(
        dates: Pair<LocalDateTime, LocalDateTime>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        if (!sahhaIsConfigured()) {
            callback?.invoke(SahhaErrors.sahhaNotConfigured, null)
            return
        }

        sim.userData.analyze(dates, callback)
    }

    fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?) {
        if (!sahhaIsConfigured()) {
            callback?.invoke(SahhaErrors.sahhaNotConfigured, null)
            return
        }

        sim.userData.getDemographic(callback)
    }

    fun postDemographic(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        if (!sahhaIsConfigured()) {
            callback?.invoke(SahhaErrors.sahhaNotConfigured, false)
            return
        }

        sim.userData.postDemographic(sahhaDemographic, callback)
    }

    fun openAppSettings(context: Context) {
        if (!sahhaIsConfigured()) {
            Log.w(tag, SahhaErrors.sahhaNotConfigured)
            return
        }

        sim.permission.openAppSettings(context)
    }

    fun enableSensors(
        context: Context,
        sensors: Set<SahhaSensor>,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        if (!sahhaIsConfigured()) {
            callback(SahhaErrors.sahhaNotConfigured, SahhaSensorStatus.pending)
            return
        }

        if (sensors.isEmpty()) {
            callback(SahhaErrors.sensorSetEmpty, SahhaSensorStatus.pending)
            return
        }

        di.defaultScope.launch {
            Session.sensors = sensors
            sim.saveConfiguration(
                Session.sensors,
                Session.settings ?: SahhaSettings(environment = SahhaEnvironment.sandbox)
            )
            sim.permission.enableSensors(context, callback)
        }
    }

    fun getSensorStatus(
        context: Context,
        sensors: Set<SahhaSensor>,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        if (!sahhaIsConfigured()) {
            callback(SahhaErrors.sahhaNotConfigured, SahhaSensorStatus.pending)
            return
        }

        if (sensors.isEmpty()) {
            callback(SahhaErrors.sensorSetEmpty, SahhaSensorStatus.pending)
            return
        }

        sim.permission.getSensorStatus(context, sensors ?: SahhaSensor.values().toSet(), callback)
    }

    fun postError(
        framework: SahhaFramework,
        message: String,
        path: String,
        method: String,
        body: String? = null,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        if (!sahhaIsConfigured()) {
            callback?.invoke(SahhaErrors.sahhaNotConfigured, false)
            return
        }

        sim.postAppError(framework, message, path, method, body, callback)
    }

    private fun sahhaIsConfigured(): Boolean {
        if (!diInitialized()) return false
        return simInitialized()
    }
}