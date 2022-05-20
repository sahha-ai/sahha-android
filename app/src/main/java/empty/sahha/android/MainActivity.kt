package empty.sahha.android

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import empty.sahha.android.ui.theme.SahhasdkemptyTheme
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.source.*
import java.time.LocalDateTime
import java.util.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = SahhaSettings(
            environment = SahhaEnvironment.development
        )
        Sahha.configure(application, config)

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
                                var permissionStatus: Enum<SahhaSensorStatus> by remember {
                                    mutableStateOf(
                                        SahhaSensorStatus.unavailable
                                    )
                                }
                                var manualPost by remember { mutableStateOf("") }
                                var manualPostDevice by remember { mutableStateOf("") }
                                var analyzeResponse by remember { mutableStateOf("") }
                                var postDemo by remember { mutableStateOf("") }
                                var getDemo by remember { mutableStateOf("") }
                                var token by remember { mutableStateOf("") }
                                var refreshToken by remember { mutableStateOf("") }

                                Sahha.getSensorStatus(
                                    this@MainActivity,
                                    SahhaSensor.sleep
                                ) { error, sensorStatus ->
                                    permissionStatus = sensorStatus
                                }

                                Greeting(greeting)
                                Spacer(modifier = Modifier.padding(16.dp))
                                Text(permissionStatus.name)
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.enableSensor(
                                        this@MainActivity,
                                        SahhaSensor.sleep
                                    ) { error, newStatus ->
                                        permissionStatus = newStatus
                                        error?.also { permissionStatus.name }
                                    }
                                }) {
                                    Text("Permission Test")
                                }
                                Spacer(modifier = Modifier.padding(16.dp))
                                OutlinedTextField(value = token, onValueChange = {
                                    token = it
                                }, label = {
                                    Text("Token")
                                })
                                OutlinedTextField(value = refreshToken, onValueChange = {
                                    refreshToken = it
                                }, label = {
                                    Text("Refresh Token")
                                })
                                Button(onClick = {
                                    Sahha.authenticate(
                                        token,
                                        refreshToken
                                    ) { error, success ->
                                        if (success) greeting = "Successful" else greeting =
                                            error ?: "Failed"
                                    }
                                }) {
                                    Text("Authenticate")
                                }
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.openAppSettings(this@MainActivity)
                                }) {
                                    Text("Open Settings")
                                }
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.start { error, success ->
                                        if (success) greeting = "Successful test start"
                                        error?.also { greeting = it }
                                    }
                                }) {
                                    Text("Test start")
                                }
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    manualPost = ""
                                    Sahha.postSensorData { error, success ->
                                        if (success) manualPost = "Successful" else manualPost =
                                            error ?: "Failed"
                                    }
                                }) {
                                    Text("Manual Post All")
                                }
                                Text(manualPost)
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    analyzeResponse = ""
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                                        analyzeResponse = SahhaErrors.androidVersionTooLow(8)
                                        return@Button
                                    }

                                    val now = Date()
                                    val lastWeek = Date(now.time - (1000*60*60*24*7))
                                    Sahha.analyze(
                                    ) { error, success ->
                                        error?.also { analyzeResponse = it }
                                        success?.also {
                                            analyzeResponse = it
                                        }
                                    }
                                }) {
                                    Text("Analyze")
                                }
                                Text(analyzeResponse)
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    postDemo = ""
                                    Sahha.postDemographic(
                                        SahhaDemographic(
                                            31, "Male", "NZ", "KR"
                                        )
                                    ) { error, success ->
                                        if (success)
                                            postDemo = "Successful"
                                        else
                                            error?.also { postDemo = it }
                                    }
                                }) {
                                    Text("Post Demographic")
                                }
                                Text(postDemo)
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.getDemographic { error, demographic ->
                                        error?.also { getDemo = it }
                                        demographic?.also {
                                            getDemo =
                                                "${it.age}, ${it.gender}, ${it.country}, ${it.birthCountry}"
                                        }
                                    }
                                }) {
                                    Text("Get Demographic")
                                }
                                Text(getDemo)
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