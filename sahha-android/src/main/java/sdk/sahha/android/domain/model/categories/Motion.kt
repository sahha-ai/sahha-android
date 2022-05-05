package sdk.sahha.android.domain.model.categories

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.domain.use_case.permissions.ActivateUseCase
import sdk.sahha.android.domain.use_case.permissions.OpenAppSettingsUseCase
import sdk.sahha.android.domain.use_case.permissions.SetPermissionLogicUseCase
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject
import javax.inject.Named

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
        context: Context,
        _activityCallback: ((error: String?, sahhaSensorStatus: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        activateUseCase(context, _activityCallback)
    }
}