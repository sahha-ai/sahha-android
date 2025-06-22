package empty.sahha.android.views

import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import sdk.sahha.android.source.Sahha

private const val APP_ID = "my_app_id"
private const val APP_SECRET = "my_app_secret"
private const val EXTERNAL_ID = "my_external_id"

@Composable
fun AuthenticationView(sharedPrefs: SharedPreferences) {
    val focusManager = LocalFocusManager.current
    var appId by remember { mutableStateOf(sharedPrefs.getString(APP_ID, null) ?: "") }
    var appSecret by remember { mutableStateOf(sharedPrefs.getString(APP_SECRET, null) ?: "") }
    var externalId by remember { mutableStateOf(sharedPrefs.getString(EXTERNAL_ID, null) ?: "") }
    var authStatus by remember { mutableStateOf("Pending") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(authStatus)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = appId,
            singleLine = true,
            onValueChange = { appId = it },
            label = { Text("App ID") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = appSecret,
            singleLine = true,
            onValueChange = { appSecret = it },
            label = { Text("App Secret") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = externalId,
            singleLine = true,
            onValueChange = { externalId = it },
            label = { Text("External ID") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            authStatus = "Loading..."

            sharedPrefs.edit()
                .putString(APP_ID, appId)
                .putString(APP_SECRET, appSecret)
                .putString(EXTERNAL_ID, externalId)
                .apply()

            Sahha.authenticate(appId, appSecret, externalId) { error, success ->
                authStatus = if (success) "Successful" else error ?: "Failed"
            }
        }) {
            Text("Authenticate")
        }

        Button(onClick = {
            authStatus = "Loading..."

            Sahha.deauthenticate { err, success ->
                err?.also {
                    authStatus = it
                    return@deauthenticate
                }

                authStatus = "De-auth successful: $success"
            }
        }) {
            Text("De-authenticate")
        }

    }
}