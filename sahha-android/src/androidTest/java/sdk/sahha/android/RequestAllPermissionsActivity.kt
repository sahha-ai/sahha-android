package sdk.sahha.android

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord

class RequestAllPermissionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        val contract = PermissionController.createRequestPermissionResultContract()
        val request = registerForActivityResult(contract) {
            finish()
        }
        request.launch(
            setOf(
                HealthPermission.getWritePermission(StepsRecord::class),
            )
        )
    }
}