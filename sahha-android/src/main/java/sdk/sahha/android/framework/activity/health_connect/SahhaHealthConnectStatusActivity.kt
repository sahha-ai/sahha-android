package sdk.sahha.android.framework.activity.health_connect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensorStatus

internal class SahhaHealthConnectStatusActivity : AppCompatActivity() {
    private val permissionHandler by lazy { Sahha.di.permissionHandler }
    private val healthConnectClient by lazy { Sahha.di.healthConnectClient }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            SahhaReconfigure(this@SahhaHealthConnectStatusActivity)
            healthConnectClient?.also { client ->
                checkPermissions(client)
            } ?: healthConnectUnavailable()
        }
    }

    private suspend fun checkPermissions(healthConnectClient: HealthConnectClient) {
        val hcPermissions = Sahha.di.permissionManager.getHcPermissions()
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(hcPermissions)) {
            permissionHandler.activityCallback.statusCallback
                ?.invoke(null, SahhaSensorStatus.enabled)
            finish()
            return
        }

        // Else
        permissionHandler.activityCallback.statusCallback
            ?.invoke(null, SahhaSensorStatus.disabled)
        finish()
    }

    private fun healthConnectUnavailable() {
        permissionHandler.activityCallback.statusCallback
            ?.invoke(null, SahhaSensorStatus.unavailable)
        finish()
    }
}