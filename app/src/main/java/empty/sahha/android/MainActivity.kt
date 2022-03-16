package empty.sahha.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import empty.sahha.android.ui.theme.SahhasdkemptyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sdk.sahha.android.domain.model.enums.PermissionStatus
import sdk.sahha.android.presentation.Sahha

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Sahha.configure(this)

        setContent {
            SahhasdkemptyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val greeting by remember { mutableStateOf("Android") }
                            var permission by remember { mutableStateOf("${PermissionStatus.unknown.ordinal}: ${PermissionStatus.unknown.name}") }

                            Greeting(greeting)
                            Spacer(modifier = Modifier.padding(16.dp))
                            Text(permission)
                            Spacer(modifier = Modifier.padding(16.dp))
                            Button(onClick = {
                                Sahha.activityRecognition.activate { permissionStatus ->
                                    permission =
                                        "${permissionStatus.ordinal}: ${permissionStatus.name}"
                                }
                            }) {
                                Text("Permission Test")
                            }
                            Spacer(modifier = Modifier.padding(16.dp))
                            Button(onClick = {
                                Sahha.authenticate(
                                    "testCustomer",
                                    "testProfile"
                                )

                                val ioScope = CoroutineScope(IO)
                                ioScope.launch {
                                    delay(1000)
                                }
                            }) {
                                Text("Authenticate")
                            }
                            Spacer(modifier = Modifier.padding(16.dp))
                            Button(onClick = {
                                Sahha.openAppSettings()
                            }) {
                                Text("Open Settings")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SahhasdkemptyTheme {
        Greeting("Android")
    }
}