package sdk.sahha.android

import androidx.activity.ComponentActivity
import androidx.annotation.Keep
import kotlinx.coroutines.launch
import sdk.sahha.android.di.ManualDependencies
import sdk.sahha.android.domain.model.categories.Motion
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.model.enums.SahhaEnvironment
import sdk.sahha.android.domain.model.enums.SahhaSensor

@Keep
object Sahha {
    internal lateinit var di: ManualDependencies
    internal val notifications by lazy { di.notifications }

    val timeManager by lazy { di.timeManager }
    val motion by lazy {
        Motion(
            di.setPermissionLogicUseCase,
            di.activateUseCase,
            di.promptUserToActivateUseCase
        )
    }

    fun configure(
        activity: ComponentActivity,
        environment: Enum<SahhaEnvironment>,
        sensorArray: Array<Enum<SahhaSensor>>,
        autoPostData: Boolean
    ) {
        di = ManualDependencies(activity)
        di.setPermissionLogicUseCase()
        di.ioScope.launch {
            saveConfiguration(environment, sensorArray, autoPostData)
        }
    }

    fun authenticate(customerId: String, profileId: String, callback: ((value: String) -> Unit)) {
        di.authenticateUseCase(customerId, profileId, callback)
    }

    fun startDataCollectionService(
        icon: Int? = null,
        title: String? = null,
        shortDescription: String? = null
    ) {
        di.startDataCollectionServiceUseCase(icon, title, shortDescription)
    }

    fun sendSleepData(callback: ((responseSuccessful: Boolean) -> Unit)) {
        di.ioScope.launch {
            di.sendSleepDataUseCase(callback)
        }
    }

    private suspend fun saveConfiguration(
        environment: Enum<SahhaEnvironment>,
        sensorArray: Array<Enum<SahhaSensor>>,
        autoPostData: Boolean
    ) {
        val sensorEnums = convertToEnums(sensorArray)
        di.configurationDao.saveConfig(
            SahhaConfiguration(
                environment.ordinal,
                sensorEnums,
                autoPostData
            )
        )
    }

    private fun convertToEnums(sensorArray: Array<Enum<SahhaSensor>>): ArrayList<Int> {
        val sensorEnums = arrayListOf<Int>()
        sensorArray.forEach {
            sensorEnums.add(it.ordinal)
        }
        return sensorEnums
    }
}