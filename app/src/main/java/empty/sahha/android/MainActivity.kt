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
import sdk.sahha.android.Sahha
import sdk.sahha.android.domain.model.config.SahhaSettings
import sdk.sahha.android.domain.model.enums.SahhaActivityStatus
import sdk.sahha.android.domain.model.enums.SahhaEnvironment
import sdk.sahha.android.domain.model.enums.SahhaSensor
import sdk.sahha.android.domain.model.profile.SahhaDemographic

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = SahhaSettings(
            environment = SahhaEnvironment.development
        )
        Sahha.configure(this, config)

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
                                var permission by remember { mutableStateOf(SahhaActivityStatus.pending.name) }
                                var manualPost by remember { mutableStateOf("") }
                                var manualPostDevice by remember { mutableStateOf("") }
                                var analyzeResponse by remember { mutableStateOf("") }
                                var postDemo by remember { mutableStateOf("") }
                                var getDemo by remember { mutableStateOf("") }

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
                                        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJQcm9maWxlSWQiOiJiZTgyNDQ5ZC1mMzUwLTQ1ZWEtYTkzMy1iYjAzZjJmNDJlOTQiLCJBY2NvdW50SWQiOiI5OTQwOGZhZS1lZGUzLTQ3MGUtYTFmYS1mZWU5YWZjZTJhMGUiLCJleHAiOjE2NTI5Mjg1NzksImlzcyI6Imh0dHBzOi8vc2FuZGJveC1hcGkuc2FoaGEuYWkiLCJhdWQiOiJodHRwczovL3NhbmRib3gtYXBpLnNhaGhhLmFpIn0.Pk3pT0ghSQP23mLr_ljCGNIqdaB98sKa_lL3yL8muhY",
                                        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJQcm9maWxlSWQiOiJiZTgyNDQ5ZC1mMzUwLTQ1ZWEtYTkzMy1iYjAzZjJmNDJlOTQiLCJBY2NvdW50SWQiOiI5OTQwOGZhZS1lZGUzLTQ3MGUtYTFmYS1mZWU5YWZjZTJhMGUiLCJleHAiOjE2NTI5Mjg1NzksImlzcyI6Imh0dHBzOi8vc2FuZGJveC1hcGkuc2FoaGEuYWkiLCJhdWQiOiJodHRwczovL3NhbmRib3gtYXBpLnNhaGhhLmFpIn0.Pk3pT0ghSQP23mLr_ljCGNIqdaB98sKa_lL3yL8muhY"
                                    ) { error, success ->
                                        if (success) greeting = "Successful" else greeting = error ?: "Failed"
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
                                    Sahha.motion.postSensorData(SahhaSensor.sleep) { error, success ->
                                        if(success) manualPost = "Successful" else manualPost = error ?: "Failed"
                                    }
                                }) {
                                    Text("Manual Post Sleep")
                                }
                                Text(manualPost)
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.device.postSensorData(SahhaSensor.device) { error, success ->
                                        if(success) manualPostDevice = "Successful" else manualPostDevice = error ?: "Failed"
                                    }
                                }) {
                                    Text("Manual Post Device")
                                }
                                Text(manualPostDevice)
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.analyze { error, success ->
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
                                    Sahha.postDemographic(
                                        SahhaDemographic(
                                            10, "m", "nz", "korea"
                                        )
                                    ) { error, success ->
                                        error?.also { postDemo = it }
                                        success?.also { postDemo = it }
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