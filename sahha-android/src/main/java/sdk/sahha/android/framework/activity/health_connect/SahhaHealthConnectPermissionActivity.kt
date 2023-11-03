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

class SahhaHealthConnectPermissionActivity : AppCompatActivity() {
    private var initialLaunch = true
    private var status = SahhaSensorStatus.pending
    private val permissionHandler by lazy { Sahha.di.permissionHandler }
    private val healthConnectClient by lazy { Sahha.di.healthConnectClient }
    private val permissions by lazy { Sahha.di.permissionManager.permissions }

    // Create the permissions launcher.
    private val requestPermissionActivityContract =
        PermissionController.createRequestPermissionResultContract()

    private val requestPermissions =
        registerForActivityResult(requestPermissionActivityContract) { granted ->
            status = if (granted.containsAll(granted)) SahhaSensorStatus.enabled
            else SahhaSensorStatus.disabled
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println("SahhaHealthConnectPermissionActivity0001")
        healthConnectClient?.also { client ->
            lifecycleScope.launch {
                println("SahhaHealthConnectPermissionActivity0002")
                checkPermissionsAndRun(client)
            }
        } ?: returnStatusAndFinish(SahhaSensorStatus.unavailable)
    }

    override fun onResume() {
        super.onResume()
        if (initialLaunch) {
            println("SahhaHealthConnectPermissionActivity0009")
            initialLaunch = false
            return
        }

        // Else
        enabledStatus()
    }

    private suspend fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient) {
        println("SahhaHealthConnectPermissionActivity0003")
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(permissions)) {
            println("SahhaHealthConnectPermissionActivity0004")
            status = SahhaSensorStatus.enabled
            enabledStatus()
            return
        }

        // Else
        println("SahhaHealthConnectPermissionActivity0005")
        requestPermissions.launch(permissions)
    }

    private fun enabledStatus() {
        if (status == SahhaSensorStatus.enabled) {
            println("SahhaHealthConnectPermissionActivity0006")
            Sahha.di.sahhaNotificationManager.startForegroundService(HealthConnectPostService::class.java)
        }

        println("SahhaHealthConnectPermissionActivity0007")
        permissionHandler.activityCallback.statusCallback?.invoke(
            null, status
        )

        println("SahhaHealthConnectPermissionActivity0008")
        finish()
    }

    private fun returnStatusAndFinish(status: Enum<SahhaSensorStatus>) {
        permissionHandler.activityCallback.statusCallback
            ?.invoke(null, status)
        finish()
    }
}