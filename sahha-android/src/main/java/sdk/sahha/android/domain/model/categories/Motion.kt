package sdk.sahha.android.domain.model.categories

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.annotation.Keep
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaErrors
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