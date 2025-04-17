package sdk.sahha.android.framework.activity.health_connect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.common.Session
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

        lifecycleScope.launch {
            healthConnectClient?.also { client ->
                Sahha.di.permissionManager.getTrimmedHcPermissions(
                    Sahha.di.permissionManager.getManifestPermissions(context = this@SahhaHealthConnectPermissionActivity),
                    Session.sensors ?: setOf()
                ) { _, _, hcPermissions ->
                    permissions = hcPermissions
                    checkPermissionsAndRun(client)
                }
            } ?: returnStatusAndFinish(SahhaSensorStatus.unavailable)
        }
    }

    override fun onResume() {
        super.onResume()
        if (initialLaunch) {
            initialLaunch = false
            return
        }

        lifecycleScope.launch {
            // Else
            enabledStatus()
        }
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

    private suspend fun enabledStatus() = coroutineScope {
        permissionHandler.activityCallback.statusCallback?.invoke(
            null, status
        )

        finish()
    }

    private suspend fun returnStatusAndFinish(status: Enum<SahhaSensorStatus>) = coroutineScope {
        permissionHandler.activityCallback.statusCallback
            ?.invoke(null, status)
        finish()
    }
}