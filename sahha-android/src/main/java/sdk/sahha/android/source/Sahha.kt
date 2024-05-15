package sdk.sahha.android.source

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.di.AppComponent
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.di.DaggerAppComponent
import sdk.sahha.android.domain.interaction.SahhaInteractionManager
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
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
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        if (!sahhaIsConfigured()) {
            callback(SahhaErrors.sahhaNotConfigured, SahhaSensorStatus.pending)
            return
        }

        sim.permission.enableSensors(context, callback)
    }

    fun getSensorStatus(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        if (!sahhaIsConfigured()) {
            callback(SahhaErrors.sahhaNotConfigured, SahhaSensorStatus.pending)
            return
        }

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

    fun enableAppUsage(context: Context) {
        di.permissionManager.appUsageSettings(context = context)
    }

    fun getAppUsageStatus(context: Context): Enum<SahhaSensorStatus> {
        return di.permissionManager.getAppUsageStatus(context = context)
    }

    fun storeAppUsages() {
        val now = ZonedDateTime.now()
        val events = di.appUsageRepo.getEvents(
            ZonedDateTime.of(
                now.minusDays(1).toLocalDate(),
                LocalTime.MIDNIGHT,
                now.zone
            ).toInstant().toEpochMilli(),
            ZonedDateTime.of(
                now.minusDays(1).toLocalDate(),
                LocalTime.of(23, 59, 59, 99),
                now.zone
            ).toInstant().toEpochMilli()
        )
        di.defaultScope.launch {
            di.batchedDataRepo.saveBatchedData(events)
        }
    }
}