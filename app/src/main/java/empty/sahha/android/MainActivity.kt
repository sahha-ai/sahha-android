package empty.sahha.android

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaDemographic
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaFramework
import sdk.sahha.android.source.SahhaNotificationConfiguration
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSettings
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
import kotlin.random.Random

const val SEVEN_DAYS_MILLIS = 604800000L

private const val MY_SHARED_PREFS = "my_shared_prefs"
private const val APP_ID = "my_app_id"
private const val APP_SECRET = "my_app_secret"
private const val EXTERNAL_ID = "my_external_id"

class MainActivity : ComponentActivity() {
    private val mainScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences(MY_SHARED_PREFS, MODE_PRIVATE)

        val config = SahhaSettings(
            environment = SahhaEnvironment.sandbox,
            notificationSettings = SahhaNotificationConfiguration(
                icon = androidx.appcompat.R.drawable.abc_btn_check_to_on_mtrl_015,
//                title = "Foreground Service",
//                shortDescription = "This mainly handles native steps and screen locks"
            ),
//            sensors = setOf(
//                SahhaSensor.heart_rate,
//                SahhaSensor.step_count,
//                SahhaSensor.sleep,
//                SahhaSensor.total_energy_burned,
//                SahhaSensor.exercise
//            )
//            sensors = setOf()
        )

//        val sensors = null
        val sensors = setOf<SahhaSensor>(
//            SahhaSensor.device_lock,
//            SahhaSensor.heart_rate,
//            SahhaSensor.step_count,
//            SahhaSensor.sleep,
//            SahhaSensor.total_energy_burned,
//            SahhaSensor.exercise
        )

        Sahha.configure(
            application,
            config,
        )

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
//                    var permissionStatus = "place holder"
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

                    appId = sharedPrefs.getString(APP_ID, null) ?: ""
                    appSecret = sharedPrefs.getString(APP_SECRET, null) ?: ""
                    externalId = sharedPrefs.getString(EXTERNAL_ID, null) ?: ""

                    Sahha.getSensorStatus(
                        this@MainActivity,
                        sensors
                    ) { error, sensorStatus ->
                        mainScope.launch {
                            permissionStatus = "${sensorStatus.name}${error?.let { "\n$it" } ?: ""}"
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LazyColumn(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                Spacer(modifier = Modifier.padding(16.dp))
                                Greeting(greeting)
                                Spacer(modifier = Modifier.padding(16.dp))
                                ForceCrashTestView()
                                Spacer(modifier = Modifier.padding(16.dp))
                                Text(permissionStatus)
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.getSensorStatus(
                                        this@MainActivity,
                                        sensors
                                    ) { error, status ->
                                        permissionStatus =
                                            "${status.name}${error?.let { "\n$it" } ?: ""}"
                                    }
                                }) {
                                    Text("Get Sensor Status")
                                }
                                Spacer(modifier = Modifier.padding(8.dp))
                                Button(onClick = {
                                    Sahha.enableSensors(
                                        this@MainActivity,
                                        setOf(SahhaSensor.heart_rate)
                                    ) { error, status ->
                                        permissionStatus =
                                            "${status.name}${error?.let { "\n$it" } ?: ""}"
                                    }
                                }) {
                                    Text("Grant Heart Permission")
                                }
                                Spacer(modifier = Modifier.padding(8.dp))
                                Button(onClick = {
                                    Sahha.enableSensors(
                                        this@MainActivity,
                                        setOf(SahhaSensor.sleep)
                                    ) { error, status ->
                                        permissionStatus =
                                            "${status.name}${error?.let { "\n$it" } ?: ""}"
                                    }
                                }) {
                                    Text("Grant Sleep Permission")
                                }
                                Spacer(modifier = Modifier.padding(8.dp))
                                Button(onClick = {
                                    Sahha.enableSensors(
                                        this@MainActivity,
                                        sensors
                                    ) { error, status ->
                                        permissionStatus =
                                            "${status.name}${error?.let { "\n$it" } ?: ""}"
                                    }
                                }) {
                                    Text("Grant All Permissions")
                                }
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.configure(
                                        application,
                                        config
                                    ) { error, success ->
                                        lifecycleScope.launch {
                                            Toast.makeText(
                                                this@MainActivity,
                                                error ?: "Successful $success",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }) {
                                    Text("Configure")
                                }
                                Spacer(modifier = Modifier.padding(16.dp))
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

                                    sharedPrefs.edit()
                                        .putString(APP_ID, appId)
                                        .putString(APP_SECRET, appSecret)
                                        .putString(EXTERNAL_ID, externalId)
                                        .apply()

                                    Sahha.authenticate(
                                        appId,
                                        appSecret,
                                        externalId
                                    ) { error, success ->
                                        authStatus =
                                            if (success) "Successful" else error ?: "Failed"
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

                                    Sahha.analyze(
                                        dates = Pair(LocalDateTime.now(), LocalDateTime.now()),
                                    ) { error, success ->
                                        error?.also { analyzeResponseLocalDateTime = it }
                                        success?.also {
                                            analyzeResponseLocalDateTime = it
                                        }
                                    }
                                }) {
                                    Text("Analyze")
                                }
                                Text(analyzeResponse)
                                Text(analyzeResponseDate)
                                Text(analyzeResponseLocalDateTime)
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    val rnd = Random.Default
                                    val gendersList = listOf("Male", "Female", "Gender diverse")
                                    val incomeRangeList = listOf(
                                        "Up to $15,000",
                                        "Up to $25,000",
                                        "Up to $50,000",
                                        "Up to $75,000",
                                        "Up to $100,000",
                                        "Up to $125,000",
                                        "Up to $150,000",
                                        "Up to $175,000",
                                        "Up to and over $200,000",
                                    )
                                    val educationList = listOf(
                                        "Primary",
                                        "Secondary",
                                        "Tertiary",
                                        "Masters",
                                        "Doctoral",
                                        "Trade",
                                    )
                                    val relationshipList = listOf(
                                        "Single",
                                        "Partner",
                                        "Spouse",
                                    )
                                    val localeList = listOf(
                                        "Rural",
                                        "Urban",
                                    )
                                    val livingArrangementList = listOf(
                                        "Renting",
                                        "Home owner",
                                        "Homeless",
                                    )
                                    val birthDate = rnd.nextInt(1900, 2016)
                                    postDemo = ""
                                    Sahha.postDemographic(
                                        SahhaDemographic(
                                            age = LocalDate.now().year - birthDate,
                                            gender = gendersList[rnd.nextInt(gendersList.size)],
                                            incomeRange = incomeRangeList[rnd.nextInt(
                                                incomeRangeList.size
                                            )],
                                            education = educationList[rnd.nextInt(educationList.size)],
                                            relationship = relationshipList[rnd.nextInt(
                                                relationshipList.size
                                            )],
                                            locale = localeList[rnd.nextInt(localeList.size)],
                                            livingArrangement = livingArrangementList[rnd.nextInt(
                                                livingArrangementList.size
                                            )],
                                            birthDate = "${birthDate}-01-01"
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
fun ButtonAndTextTemplate(
    buttonLabel: String,
    onClick: () -> Unit
) {
    var status by remember { mutableStateOf("Pending...") }

    Spacer(modifier = Modifier.padding(16.dp))
    Text(status)
    Button(onClick = {
        status = "Loading..."
        onClick()
    }) {
        Text(buttonLabel)
    }
    Spacer(modifier = Modifier.padding(16.dp))
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
fun ForceCrashTestView() {
    Spacer(modifier = Modifier.padding(16.dp))
    Button(onClick = {
        throw Exception("Crash test!")
    }) {
        Text("Force Crash Test")
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

//@Composable
//fun NotificationPermission(context: Context) {
//    Text("Notification Permission")
//    Spacer(modifier = Modifier.padding(16.dp))
//    Button(onClick = {
//        Sahha.enableNotificationsAsync(context)
//    }) {
//        Text("Enable")
//    }
//    Spacer(modifier = Modifier.padding(16.dp))
//}