package empty.sahha.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import sdk.sahha.android.domain.model.enums.SahhaActivityStatus
import sdk.sahha.android.domain.model.enums.SahhaSensor

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Sahha.configure(this, manuallyPostData = true)

        setContent {
            SahhasdkemptyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LazyColumn(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                var greeting by remember { mutableStateOf("Android") }
                                var permission by remember { mutableStateOf(SahhaActivityStatus.PENDING.name) }
                                var manualPost by remember { mutableStateOf("") }

                                Greeting(greeting)
                                Spacer(modifier = Modifier.padding(16.dp))
                                Text(permission)
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.motion.activate { newStatus ->
                                        permission = newStatus.name
                                    }
                                }) {
                                    Text("Permission Test")
                                }
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.authenticate(
                                        "testCustomer938g93i4hg9ihdfg3984hg9hd9uhg34g34g34gdsge4g",
                                        "testProfile439g8hoihd9g8h349gih34gdsg34gsdg34gseg34gsdg3g3"
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
                                    Sahha.motion.promptUserToActivate { newStatus ->
                                        permission = newStatus.name
                                    }
                                }) {
                                    Text("Open Settings")
                                }
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.start()
                                }) {
                                    Text("Test start")
                                }
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.motion.postData(SahhaSensor.SLEEP) { error, success ->
                                        error?.also {
                                            manualPost = it
                                        }
                                        success?.also {
                                            manualPost = it
                                        }
                                    }
                                }) {
                                    Text("Manual Post")
                                }
                                Text(manualPost)
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