package sdk.sahha.android.data.repository

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.common.SahhaIntents
import sdk.sahha.android.domain.model.callbacks.ActivityCallback
import sdk.sahha.android.domain.model.callbacks.WindowCallback
import sdk.sahha.android.source.SahhaActivityStatus
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
        callback: ((sahhaActivityStatus: Enum<SahhaActivityStatus>) -> Unit)
    ) {
        activityCallback.setSettingOnResume = callback
        openAppSettings()
    }

    override fun activate(callback: ((Enum<SahhaActivityStatus>) -> Unit)) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val status = SahhaActivityStatus.unavailable
            callback(status)
            Sahha.motion.activityStatus = status
            return
        }

        activityCallback.requestPermission = callback
        permission.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
    }

    private fun openAppSettings() {
        val openSettingsIntent = SahhaIntents.settings()
        activity.startActivity(openSettingsIntent)
    }

    private fun convertToActivityStatus(enabled: Boolean): Enum<SahhaActivityStatus> {
        if (enabled) return SahhaActivityStatus.enabled
        else return SahhaActivityStatus.disabled
    }

    private fun setWindowFocusCallback() {
        val win = activity.window
        val localCallback = win.callback
        win.callback =
            WindowCallback(localCallback, activityCallback)
        (win.callback as WindowCallback).onWindowFocusChanged(win.isActive)
    }
}