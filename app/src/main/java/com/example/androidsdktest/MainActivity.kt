package com.example.androidsdktest

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.Sahha
import com.example.androidsdktest.ui.theme.SahhasdkemptyTheme
import core.config.SahhaEnvironment
import core.config.SahhaNotificationConfiguration
import core.config.SahhaSettings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure Sahha SDK with development (sandbox) environment
        val notificationSettings = SahhaNotificationConfiguration(
            icon = R.drawable.ic_launcher_foreground, // Replace with your actual icon resource
            title = "Sahha Notification",
            shortDescription = "Sahha is running in the background"
        )
        val settings = SahhaSettings(
            environment = SahhaEnvironment.DEVELOPMENT, // Development environment
            notificationSettings = notificationSettings
        )
        Sahha.configure(this, settings) { error, success ->
            if (error != null) {
                Toast.makeText(this, "Configuration error: $error", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Configuration success: $success", Toast.LENGTH_LONG).show()
            }
        }

        setContent {
            SahhasdkemptyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello Android!",
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = {
            // Replace with your actual appId and appSecret
            val appId = "your_app_id_here"
            val appSecret = "your_app_secret_here"
            val externalId: String? = null // Optional

            Sahha.authenticate(appId, appSecret, externalId) { error, success ->
                // Handle callback; in Compose, this is fine as it's fire-and-forget
                // For UI updates, use state if needed
                println(error ?: "Authentication success: $success")
            }
        }) {
            Text("Register Account")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SahhasdkemptyTheme {
        Greeting()
    }
}