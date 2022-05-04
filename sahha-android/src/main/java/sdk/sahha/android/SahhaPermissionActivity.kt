package sdk.sahha.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.ui.theme.SahhasdkemptyTheme

class SahhaPermissionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Sahha.motion.prepareActivity(this)
        Sahha.motion.activate { _, _ -> finish() }

        setContent {
            SahhasdkemptyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background.copy(alpha = 0f),
                ) {}
            }
        }
    }
}