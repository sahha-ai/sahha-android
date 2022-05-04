package sdk.sahha.android.domain.model.categories

import android.content.Context
import androidx.annotation.Keep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.domain.use_case.permissions.ActivateUseCase
import sdk.sahha.android.domain.use_case.permissions.OpenAppSettingsUseCase
import sdk.sahha.android.domain.use_case.permissions.SetPermissionLogicUseCase
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaActivityStatus
import javax.inject.Inject
import javax.inject.Named

@Keep
class Motion @Inject constructor(
    openAppSettingsUseCase: OpenAppSettingsUseCase,
    setPermissionLogicUseCase: SetPermissionLogicUseCase,
    private val configDao: ConfigurationDao,
    @Named("ioScope") private val ioScope: CoroutineScope,
    private val activateUseCase: ActivateUseCase,
) : RequiresPermission(
    setPermissionLogicUseCase,
) {
    fun testNewActivate(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaActivityStatus>) -> Unit)
    ) {
        activateUseCase.testNewActivate(context, callback)
    }

    fun activate(
        _activityCallback: ((error: String?, sahhaActivityStatus: Enum<SahhaActivityStatus>) -> Unit)
    ) {
        activateUseCase(_activityCallback)
    }

    //TODO: For demo only
    fun getData(callback: ((data: List<String>) -> Unit)) {
        Sahha.di.ioScope.launch {
            val sleepData = Sahha.di.sleepDao.getSleepDto()
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
}