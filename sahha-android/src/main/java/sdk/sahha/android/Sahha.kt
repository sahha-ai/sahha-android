package sdk.sahha.android

import androidx.activity.ComponentActivity
import androidx.annotation.Keep
import kotlinx.coroutines.launch
import sdk.sahha.android.di.ManualDependencies
import sdk.sahha.android.domain.model.categories.Device
import sdk.sahha.android.domain.model.categories.Motion
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.model.config.SahhaSettings
import sdk.sahha.android.domain.model.enums.SahhaEnvironment
import sdk.sahha.android.domain.model.enums.SahhaSensor

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
        di = ManualDependencies(activity)
        di.setPermissionLogicUseCase()
        di.ioScope.launch {
            saveConfiguration(settings.environment, settings.sensors, settings.manuallyPostData)
        }
    }

    fun authenticate(customerId: String, profileId: String, callback: ((value: String) -> Unit)) {
        di.authenticateUseCase(customerId, profileId, callback)
    }

    fun start() {
        di.defaultScope.launch {
            config = di.configurationDao.getConfig()
            startDataCollection()
            checkAndStartPostWorkers()
        }
    }

    fun analyze(callback: ((error: String?, success: String?) -> Unit)?) {
        di.defaultScope.launch {
            di.analyzeProfileUseCase(callback)
        }
    }

    //TODO: For demo only
    fun getSleepData(callback: ((data: List<String>) -> Unit)) {
        di.ioScope.launch {
            val sleepData = di.sleepDao.getSleepDto()
            val sleepDataString = mutableListOf<String>()
            sleepData.mapTo(sleepDataString) {
                "Minutes slept: ${it.minutesSlept}" +
                        "\nStarted: ${it.startDateTime}" +
                        "\nEnded: ${it.endDateTime}" +
                        "\nCreated at: ${it.createdAt}"
            }
            callback(sleepDataString)
        }
    }

    //TODO: For demo only
    fun getDeviceData(callback: ((data: List<String>) -> Unit)) {
        di.ioScope.launch {
            val lockData = di.deviceUsageDao.getUsages()
            val lockDataString = mutableListOf<String>()
            lockData.mapTo(lockDataString) {
                when {
                    it.isLocked -> {
                        "Locked at ${it.createdAt}"
                    }
                    else -> {
                        "Unlocked at ${it.createdAt}"
                    }
                }
            }
            callback(lockDataString)
        }
    }

    private fun checkAndStartPostWorkers() {
        if (!config.manuallyPostData) {
            di.startPostWorkersUseCase()
        }
    }

    private fun startDataCollection() {
        if (config.sensorArray.contains(SahhaSensor.SLEEP.ordinal)) {
            di.startCollectingSleepDataUseCase()
        }

        // Pedometer/device checkers are in the service
        startDataCollectionService()
    }

    private fun startDataCollectionService(
        icon: Int? = null,
        title: String? = null,
        shortDescription: String? = null
    ) {
        di.startDataCollectionServiceUseCase(icon, title, shortDescription)
    }

    private suspend fun saveConfiguration(
        environment: Enum<SahhaEnvironment>,
        sensorSet: Set<Enum<SahhaSensor>>,
        manuallyPostData: Boolean
    ) {
        val sensorEnums = convertToEnums(sensorSet)
        di.configurationDao.saveConfig(
            SahhaConfiguration(
                environment.ordinal,
                sensorEnums,
                manuallyPostData
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
}