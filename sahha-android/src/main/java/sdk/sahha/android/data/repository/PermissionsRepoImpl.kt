package sdk.sahha.android.data.repository

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import sdk.sahha.android.domain.model.PermissionCallback
import sdk.sahha.android.domain.model.enums.PermissionStatus
import sdk.sahha.android.domain.repository.PermissionsRepo
import javax.inject.Inject

class PermissionsRepoImpl @Inject constructor(
    private val activity: ComponentActivity
) : PermissionsRepo {
    private lateinit var permission: ActivityResultLauncher<String>

    override fun setPermissionLogic() {
        permission =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    }

    override fun setPermissionLogic(permissionCallback: PermissionCallback) {
        permission =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { enabled ->
                val status = convertToPermissionStatus(enabled)
                permissionCallback.unit(status)
            }
    }

    override fun openSettings() {
        val openSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val packageNameUri = Uri.fromParts("package", activity.packageName, null)
        openSettingsIntent.data = packageNameUri

        activity.startActivity(openSettingsIntent)
    }

    override fun grantActivityRecognition() {
        permission.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
    }

    private fun convertToPermissionStatus(enabled: Boolean): Enum<PermissionStatus> {
        if (enabled) return PermissionStatus.enabled
        else return PermissionStatus.disabled
    }
}