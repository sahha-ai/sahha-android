package sdk.sahha.android.framework.activity.health_connect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import sdk.sahha.android.framework.service.HealthConnectPostService
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensorStatus

internal class SahhaHealthConnectPermissionActivity : AppCompatActivity() {
    private lateinit var permissions: Set<String>

    private var initialLaunch = true
    private var status = SahhaSensorStatus.pending
    private val permissionHandler by lazy { Sahha.di.permissionHandler }
    private val healthConnectClient by lazy { Sahha.di.healthConnectClient }

    // Create the permissions launcher.
    private val requestPermissionActivityContract =
        PermissionController.createRequestPermissionResultContract()

    private val requestPermissions =
        registerForActivityResult(requestPermissionActivityContract) { granted ->
            status = when (granted.containsAll(permissions)) {
                true -> SahhaSensorStatus.enabled
                false -> SahhaSensorStatus.disabled
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        healthConnectClient?.also { client ->
            lifecycleScope.launch {
                permissions = Sahha.di.permissionManager.getHcPermissions()
                checkPermissionsAndRun(client)
            }
        } ?: returnStatusAndFinish(SahhaSensorStatus.unavailable)
    }

    override fun onResume() {
        super.onResume()
        if (initialLaunch) {
            initialLaunch = false
            return
        }

        // Else
        enabledStatus()
    }

    private suspend fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(permissions)) {
            status = SahhaSensorStatus.enabled
            enabledStatus()
            return
        }

        // Else
        requestPermissions.launch(permissions)
    }

    private fun enabledStatus() {
        if (status == SahhaSensorStatus.enabled) {
            Sahha.di.sahhaNotificationManager.startForegroundService(HealthConnectPostService::class.java)
        }

        permissionHandler.activityCallback.statusCallback?.invoke(
            null, status
        )

        finish()
    }

    private fun returnStatusAndFinish(status: Enum<SahhaSensorStatus>) {
        permissionHandler.activityCallback.statusCallback
            ?.invoke(null, status)
        finish()
    }
}