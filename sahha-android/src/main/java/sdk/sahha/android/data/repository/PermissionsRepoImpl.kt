package sdk.sahha.android.data.repository

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import sdk.sahha.android.common.SahhaIntents
import sdk.sahha.android.domain.model.callbacks.ActivityCallback
import sdk.sahha.android.domain.model.callbacks.WindowCallback
import sdk.sahha.android.domain.model.enums.ActivityStatus
import sdk.sahha.android.domain.repository.PermissionsRepo
import javax.inject.Inject

class PermissionsRepoImpl @Inject constructor(
    private val activity: ComponentActivity
) : PermissionsRepo {
    private lateinit var permission: ActivityResultLauncher<String>
    private val activityCallback = ActivityCallback()

    override fun setPermissionLogic() {
        permission =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { enabled ->
                val status = convertToActivityStatus(enabled)
                activityCallback.requestPermission?.let { it(status) }
            }
        setWindowFocusCallback()
    }

    override fun promptUserToActivateActivityRecognition(
        callback: ((activityStatus: Enum<ActivityStatus>) -> Unit)
    ) {
        activityCallback.setSettingOnResume = callback
        openAppSettings()
    }

    override fun activate(callback: ((Enum<ActivityStatus>) -> Unit)) {
        activityCallback.requestPermission = callback
        permission.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
    }

    private fun openAppSettings() {
        val openSettingsIntent = SahhaIntents.settings()
        activity.startActivity(openSettingsIntent)
    }

    private fun convertToActivityStatus(enabled: Boolean): Enum<ActivityStatus> {
        if (enabled) return ActivityStatus.enabled
        else return ActivityStatus.disabled
    }

    private fun setWindowFocusCallback() {
        val win = activity.window
        val localCallback = win.callback
        win.callback =
            WindowCallback(activity, localCallback, activityCallback)
        (win.callback as WindowCallback).onWindowFocusChanged(win.isActive)
    }
}