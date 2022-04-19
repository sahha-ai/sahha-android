package sdk.sahha.android

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.Keep
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import kotlinx.coroutines.launch
import sdk.sahha.android.di.ManualDependencies
import sdk.sahha.android.domain.model.categories.Device
import sdk.sahha.android.domain.model.categories.Motion
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.model.config.SahhaSettings
import sdk.sahha.android.domain.model.enums.SahhaEnvironment
import sdk.sahha.android.domain.model.enums.SahhaSensor
import sdk.sahha.android.domain.model.profile.SahhaDemographic


@Keep
object Sahha {
    private lateinit var config: SahhaConfiguration
    internal lateinit var di: ManualDependencies
    internal val notifications by lazy { di.notifications }

    val timeManager by lazy { di.timeManager }
    val motion by lazy {
        Motion(
            di.setPermissionLogicUseCase,
            di.configurationDao,
            di.ioScope,
            di.activateUseCase,
            di.promptUserToActivateUseCase,
            di.postSleepDataUseCase
        )
    }
    val device by lazy {
        Device(
            di.ioScope,
            di.configurationDao,
            di.postDeviceDataUseCase
        )
    }

    fun configure(
        activity: ComponentActivity,
        settings: SahhaSettings
    ) {
        di = ManualDependencies(activity, settings.environment)
        di.setPermissionLogicUseCase()
        di.ioScope.launch {
            saveConfiguration(settings)
            AppCenter.start(
                activity.application,
                getCorrectAppCenterKey(settings.environment),
                Analytics::class.java, Crashes::class.java
            )
        }
    }

    fun authenticate(
        token: String,
        refreshToken: String,
        callback: ((error: String?, success: String?) -> Unit)? = null
    ) {
        di.ioScope.launch {
                di.saveTokensUseCase(token, refreshToken, callback)
        }
    }

    fun start(callback: ((error: String?, success: String?) -> Unit)? = null) {
        di.defaultScope.launch {
            config = di.configurationDao.getConfig()
            startDataCollection(callback)
            checkAndStartPostWorkers()
        }
    }

    fun analyze(callback: ((error: String?, success: String?) -> Unit)?) {
        di.defaultScope.launch {
            di.analyzeProfileUseCase(callback)
        }
    }

    fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?) {
        di.defaultScope.launch {
            di.getDemographicUseCase(callback)
        }
    }

    fun postDemographic(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, success: String?) -> Unit)?
    ) {
        di.defaultScope.launch {
            di.postDemographicUseCase(sahhaDemographic, callback)
        }
    }

    private fun checkAndStartPostWorkers() {
        if (!config.manuallyPostData) {
            di.startPostWorkersUseCase()
        }
    }

    private fun startDataCollection(callback: ((error: String?, success: String?) -> Unit)?) {
        if (config.sensorArray.contains(SahhaSensor.sleep.ordinal)) {
            di.startCollectingSleepDataUseCase()
        }

        // Pedometer/device checkers are in the service
        startDataCollectionService(callback = callback)
    }

    private fun startDataCollectionService(
        icon: Int? = null,
        title: String? = null,
        shortDescription: String? = null,
        callback: ((error: String?, success: String?) -> Unit)? = null
    ) {
        if (Build.VERSION.SDK_INT < 26) callback?.also { it("Android version must be 8 or above", null) }

        di.startDataCollectionServiceUseCase(icon, title, shortDescription)
    }

    private suspend fun saveConfiguration(
        settings: SahhaSettings
    ) {
        val sensorEnums = convertToEnums(settings.sensors)
        di.configurationDao.saveConfig(
            SahhaConfiguration(
                settings.environment.ordinal,
                settings.framework.name,
                sensorEnums,
                settings.manuallyPostData
            )
        )
    }

    private fun convertToEnums(sensorSet: Set<Enum<SahhaSensor>>): ArrayList<Int> {
        val sensorEnums = arrayListOf<Int>()
        sensorSet.forEach {
            sensorEnums.add(it.ordinal)
        }
        return sensorEnums
    }

    private fun getCorrectAppCenterKey(environment: Enum<SahhaEnvironment>): String {
        if (environment == SahhaEnvironment.production) {
            return BuildConfig.APP_CENTER_PROD
        }
        return BuildConfig.APP_CENTER_DEV
    }
}