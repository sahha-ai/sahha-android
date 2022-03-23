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
import sdk.sahha.android.Sahha
import sdk.sahha.android.domain.model.enums.ActivityStatus

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
                            var greeting by remember { mutableStateOf("Android") }
                            var permission by remember { mutableStateOf(ActivityStatus.pending.name) }

                            Greeting(greeting)
                            Spacer(modifier = Modifier.padding(16.dp))
                            Text(permission)
                            Spacer(modifier = Modifier.padding(16.dp))
                            Button(onClick = {
                                Sahha.activate { newStatus ->
                                    permission = newStatus.name
                                }
                            }) {
                                Text("Permission Test")
                            }
                            Spacer(modifier = Modifier.padding(16.dp))
                            Button(onClick = {
                                Sahha.authenticate(
                                    "testCustomer",
                                    "testProfile"
                                ) { value ->
                                    greeting = value
                                }

                                val ioScope = CoroutineScope(IO)
                                ioScope.launch {
                                    delay(1000)
                                }
                            }) {
                                Text("Authenticate")
                            }
                            Spacer(modifier = Modifier.padding(16.dp))
                            Button(onClick = {
                                Sahha.promptUserToActivate { newStatus ->
                                    permission = newStatus.name
                                }
                            }) {
                                Text("Open Settings")
                            }
                            Spacer(modifier = Modifier.padding(16.dp))
                            Button(onClick = {
                                Sahha.startDataCollectionService()
                            }) {
                                Text("Start Service")
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