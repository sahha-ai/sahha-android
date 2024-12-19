package sdk.sahha.android.source

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.Keep
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.Session
import sdk.sahha.android.di.AppComponent
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.di.DaggerAppComponent
import sdk.sahha.android.domain.interaction.SahhaInteractionManager
import sdk.sahha.android.domain.internal_enum.toSahhaSensorStatus
import sdk.sahha.android.domain.model.local_logs.SahhaSample
import sdk.sahha.android.domain.model.local_logs.SahhaStat
import java.time.LocalDateTime
import java.util.Date

private const val TAG = "Sahha"

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

    private suspend fun subscribeToAppEvents(lifecycleOwner: LifecycleOwner) {
        withContext(Dispatchers.Main) {
            lifecycleOwner.lifecycle.addObserver(di.hostAppLifecycleObserver)
        }
    }

    fun configure(
        activity: ComponentActivity,
        sahhaSettings: SahhaSettings,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        saveEnvironment(activity, sahhaSettings.environment.ordinal)

        if (!diInitialized())
            di = DaggerAppComponent.builder()
                .appModule(AppModule(sahhaSettings.environment))
                .context(activity)
                .build()

        if (!simInitialized()) sim = di.sahhaInteractionManager

        di.defaultScope.launch {
            subscribeToAppEvents(activity)
            sim.configure(activity.application, sahhaSettings, callback)
        }
    }

    private fun saveEnvironment(context: Context, envInt: Int) {
        val prefs =
            context.getSharedPreferences(Constants.CONFIGURATION_PREFS, Context.MODE_PRIVATE)
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

    fun getBiomarkers(
        categories: Set<SahhaBiomarkerCategory>,
        types: Set<SahhaBiomarkerType>,
        callback: ((error: String?, value: String?) -> Unit)
    ) {
        if (!sahhaIsConfigured()) {
            callback(SahhaErrors.sahhaNotConfigured, null)
            return
        }

        di.getBiomarkersUseCase(
            categories = categories,
            types = types,
            callback = callback
        )
    }

    @JvmName("getBiomarkersDate")
    fun getBiomarkers(
        categories: Set<SahhaBiomarkerCategory>,
        types: Set<SahhaBiomarkerType>,
        dates: Pair<Date, Date>,
        callback: ((error: String?, value: String?) -> Unit)
    ) {
        if (!sahhaIsConfigured()) {
            callback(SahhaErrors.sahhaNotConfigured, null)
            return
        }

        di.getBiomarkersUseCase(
            categories = categories,
            types = types,
            dates = dates,
            callback = callback
        )
    }

    @JvmName("getBiomarkersLocalDate")
    fun getBiomarkers(
        categories: Set<SahhaBiomarkerCategory>,
        types: Set<SahhaBiomarkerType>,
        localDates: Pair<LocalDateTime, LocalDateTime>,
        callback: ((error: String?, value: String?) -> Unit)
    ) {
        if (!sahhaIsConfigured()) {
            callback(SahhaErrors.sahhaNotConfigured, null)
            return
        }

        di.getBiomarkersUseCase(
            categories = categories,
            types = types,
            localDates = localDates,
            callback = callback
        )
    }


    fun getScores(
        types: Set<SahhaScoreType>,
        callback: ((error: String?, value: String?) -> Unit)?
    ) {
        if (!sahhaIsConfigured()) {
            callback?.invoke(SahhaErrors.sahhaNotConfigured, null)
            return
        }

        sim.userData.getScores(types, callback)
    }


    @JvmName("getScoresDate")
    fun getScores(
        types: Set<SahhaScoreType>,
        dates: Pair<Date, Date>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        if (!sahhaIsConfigured()) {
            callback?.invoke(SahhaErrors.sahhaNotConfigured, null)
            return
        }

        sim.userData.getScores(types, dates, callback)
    }

    @JvmName("getScoresLocalDateTime")
    fun getScores(
        types: Set<SahhaScoreType>,
        dates: Pair<LocalDateTime, LocalDateTime>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        if (!sahhaIsConfigured()) {
            callback?.invoke(SahhaErrors.sahhaNotConfigured, null)
            return
        }

        sim.userData.getScores(types, dates, callback)
    }

    fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?) {
        if (!sahhaIsConfigured()) {
            callback?.invoke(SahhaErrors.sahhaNotConfigured, null)
            return
        }

        sim.userData.getDemographic(callback)
    }

    fun getStats(
        sensor: SahhaSensor,
        dates: Pair<LocalDateTime, LocalDateTime>,
        callback: (error: String?, stats: List<SahhaStat>?) -> Unit
    ) {
        if (!sahhaIsConfigured()) {
            callback(SahhaErrors.sahhaNotConfigured, null)
            return
        }

        di.defaultScope.launch {
            val stats = di.getStatsUseCase(
                sensor = sensor,
                interval = SahhaStatInterval.day,
                localDates = dates
            )

            callback(stats.first, stats.second)
        }
    }

    @JvmName("getStatsDate")
    fun getStats(
        sensor: SahhaSensor,
        dates: Pair<Date, Date>,
        callback: (error: String?, stats: List<SahhaStat>?) -> Unit
    ) {
        if (!sahhaIsConfigured()) {
            callback(SahhaErrors.sahhaNotConfigured, null)
            return
        }

        di.defaultScope.launch {
            val stats = di.getStatsUseCase(
                sensor = sensor,
                interval = SahhaStatInterval.day,
                dates = dates
            )

            callback(stats.first, stats.second)
        }
    }

    fun getSamples(
        sensor: SahhaSensor,
        dates: Pair<LocalDateTime, LocalDateTime>,
        callback: (error: String?, samples: List<SahhaSample>?) -> Unit
    ) {
        if (!sahhaIsConfigured()) {
            callback(SahhaErrors.sahhaNotConfigured, null)
            return
        }

        di.defaultScope.launch {
            val stats = di.getSamplesUseCase(
                sensor = sensor,
                localDates = dates
            )

            callback(stats.first, stats.second)
        }
    }

    @JvmName("getSamplesDate")
    fun getSamples(
        sensor: SahhaSensor,
        dates: Pair<Date, Date>,
        callback: (error: String?, samples: List<SahhaSample>?) -> Unit
    ) {
        if (!sahhaIsConfigured()) {
            callback(SahhaErrors.sahhaNotConfigured, null)
            return
        }

        di.defaultScope.launch {
            val stats = di.getSamplesUseCase(
                sensor = sensor,
                dates = dates
            )

            callback(stats.first, stats.second)
        }
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
            Log.w(TAG, SahhaErrors.sahhaNotConfigured)
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

        di.defaultScope.launch {
            sim.permission.processMultipleSensors(context, sensors) { error, status ->
                callback(error, status.toSahhaSensorStatus())
            }
        }
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