package empty.sahha.android

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import empty.sahha.android.ui.theme.SahhasdkemptyTheme
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaDemographic
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaFramework
import sdk.sahha.android.source.SahhaNotificationConfiguration
import sdk.sahha.android.source.SahhaSettings
import java.time.LocalDateTime
import java.util.Date

const val SEVEN_DAYS_MILLIS = 604800000L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = SahhaSettings(
            environment = SahhaEnvironment.sandbox,
            notificationSettings = SahhaNotificationConfiguration(
                icon = androidx.appcompat.R.drawable.abc_btn_check_to_on_mtrl_015,
                title = "Foreground Service",
                shortDescription = "This mainly handles the steps and screen locks"
            )
        )

        Sahha.configure(
            application,
            config,
        ) { error, success ->
            Toast.makeText(this, error ?: "Successful $success", Toast.LENGTH_LONG).show()
        }

        setContent {
            SahhasdkemptyTheme {
                val lfm = LocalFocusManager.current

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colors.background,
                ) {
                    var greeting by remember { mutableStateOf("Android") }
                    var permissionStatus by remember { mutableStateOf("") }
                    var manualPost by remember { mutableStateOf("") }
                    var manualPostDevice by remember { mutableStateOf("") }
                    var analyzeResponse by remember { mutableStateOf("") }
                    var analyzeResponseEpoch by remember { mutableStateOf("") }
                    var analyzeResponseDate by remember { mutableStateOf("") }
                    var analyzeResponseLocalDateTime by remember { mutableStateOf("") }
                    var postDemo by remember { mutableStateOf("") }
                    var getDemo by remember { mutableStateOf("") }
                    var appId by remember { mutableStateOf("") }
                    var appSecret by remember { mutableStateOf("") }
                    var externalId by remember { mutableStateOf("") }
                    var start by remember { mutableStateOf("") }
                    var authStatus by remember { mutableStateOf("Pending") }

                    Sahha.getSensorStatus(
                        this@MainActivity
                    ) { error, sensorStatus ->
                        permissionStatus = sensorStatus.name
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LazyColumn(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                Greeting(greeting)
                                Spacer(modifier = Modifier.padding(16.dp))
                                Text(permissionStatus)
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.enableSensors(
                                        this@MainActivity,
                                    ) { error, status ->
                                        permissionStatus = status.name
                                    }
                                }) {
                                    Text("Permission Test")
                                }
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.configure(
                                        application,
                                        config
                                    ) { error, success ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            error ?: "Successful $success",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }) {
                                    Text("Configure")
                                }
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    lifecycleScope.launch {
                                        SahhaReconfigure(application)
                                    }
                                }) {
                                    Text("Reconfigure")
                                }
                                ErrorLogView()
                                DeauthenticateView()
                                Spacer(modifier = Modifier.padding(16.dp))
                                Text(authStatus)
                                OutlinedTextField(
                                    value = appId,
                                    singleLine = true,
                                    onValueChange = {
                                        appId = it
                                    }, label = {
                                        Text("App ID")
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        lfm.clearFocus()
                                    })
                                )
                                OutlinedTextField(
                                    value = appSecret,
                                    singleLine = true,
                                    onValueChange = {
                                        appSecret = it
                                    }, label = {
                                        Text("App Secret")
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        lfm.clearFocus()
                                    })
                                )
                                OutlinedTextField(
                                    value = externalId,
                                    singleLine = true,
                                    onValueChange = {
                                        externalId = it
                                    }, label = {
                                        Text("External ID")
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        lfm.clearFocus()
                                    })
                                )
                                Button(onClick = {
                                    authStatus = "Loading..."

                                    Sahha.authenticate(
                                        appId,
                                        appSecret,
                                        externalId
                                    ) { error, success ->
                                        if (success) authStatus = "Successful" else authStatus =
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

                                    val now = Date()
                                    val lastWeek = Date(now.time - SEVEN_DAYS_MILLIS)

                                    Sahha.analyze { error, success ->
                                        error?.also { analyzeResponse = it }
                                        success?.also {
                                            analyzeResponse = it
                                        }
                                    }

                                    Sahha.analyze(
                                        dates = Pair(lastWeek, now),
                                    ) { error, success ->
                                        error?.also { analyzeResponseDate = it }
                                        success?.also {
                                            analyzeResponseDate = it
                                        }
                                    }

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        Sahha.analyze(
                                            dates = Pair(LocalDateTime.now(), LocalDateTime.now()),
                                        ) { error, success ->
                                            error?.also { analyzeResponseLocalDateTime = it }
                                            success?.also {
                                                analyzeResponseLocalDateTime = it
                                            }
                                        }
                                    } else {
                                        analyzeResponseLocalDateTime =
                                            SahhaErrors.androidVersionTooLow(8)
                                    }
                                }) {
                                    Text("Analyze")
                                }
                                Text(analyzeResponse)
                                Text(analyzeResponseDate)
                                Text(analyzeResponseLocalDateTime)
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    postDemo = ""
                                    Sahha.postDemographic(
                                        SahhaDemographic(
                                            31,
                                            "Male",
                                            "NZ",
                                            "KR",
                                            "South Korean",
                                            "Software Developer",
                                            "Information Technology",
                                            "$40K - $69K",
                                            "Tertiary",
                                            "Spouse",
                                            "Urban",
                                            "Renting",
                                            "1990-01-01"
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
                                                "${it.age}, ${it.gender}, ${it.country}, ${it.birthCountry}, ${it.ethnicity}, ${it.occupation}, ${it.industry}, ${it.incomeRange}, ${it.education}, ${it.relationship}, ${it.locale}, ${it.livingArrangement}, ${it.birthDate}"
                                        }
                                    }
                                }) {
                                    Text("Get Demographic")
                                }
                                Text(getDemo)
                                Spacer(modifier = Modifier.padding(16.dp))
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

@Composable
fun DeauthenticateView() {
    var status by remember { mutableStateOf("Pending...") }

    Spacer(modifier = Modifier.padding(16.dp))
    Text(status)
    Button(onClick = {
        status = "Loading..."

        Sahha.deauthenticate { err, success ->
            err?.also {
                status = it
                return@deauthenticate
            }

            status = "De-auth successful: $success"
        }
    }) {
        Text("De-authenticate")
    }
}

@Composable
fun ErrorLogView() {
    var status by remember { mutableStateOf("Pending...") }
    var codeMethod by remember { mutableStateOf("") }
    var codePath by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var codeBody by remember { mutableStateOf("") }

    Spacer(modifier = Modifier.padding(16.dp))
    Text(status)

    MyOutlinedTextField(
        textLabel = "Message",
        textValue = message,
        onValueChange = { newValue -> message = newValue })
    MyOutlinedTextField(
        textLabel = "Path",
        textValue = codePath,
        onValueChange = { newValue -> codePath = newValue })
    MyOutlinedTextField(
        textLabel = "Method",
        textValue = codeMethod,
        onValueChange = { newValue -> codeMethod = newValue })
    MyOutlinedTextField(
        textLabel = "Body?",
        textValue = codeBody,
        onValueChange = { newValue -> codeBody = newValue })

    Button(onClick = {
        status = "Loading..."

        Sahha.postError(
            SahhaFramework.android_kotlin,
            message,
            codePath,
            codeMethod,
            codeBody
        ) { err, success ->
            err?.also {
                status = it
                return@postError
            }

            status = "Error post successful: $success"
        }
    }) {
        Text("Post Error")
    }
}

@Composable
fun MyOutlinedTextField(
    textLabel: String,
    textValue: String,
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    lfm: FocusManager = LocalFocusManager.current,
    onValueChange: (newValue: String) -> Unit
) {

    OutlinedTextField(
        value = textValue,
        singleLine = singleLine,
        onValueChange = {
            onValueChange(it)
        }, label = {
            Text(textLabel)
        },
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = {
            lfm.clearFocus()
        })
    )
}