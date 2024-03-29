package sdk.sahha.android.framework.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import sdk.sahha.android.source.Sahha

internal class SahhaNotificationPermissionActivity : AppCompatActivity() {
    private val permissionManager by lazy { Sahha.di.permissionManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager.enableNotifications(this) { finish() }
    }
}