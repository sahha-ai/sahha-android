package empty.sahha.android.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaFramework

@Composable
fun ErrorLogView() {
    var status by remember { mutableStateOf("Pending...") }
    var codeMethod by remember { mutableStateOf("") }
    var codePath by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var codeBody by remember { mutableStateOf("") }

    Column {
        Spacer(modifier = Modifier.padding(16.dp))
        Text(status)

        MyOutlinedTextField(
            textLabel = "Message",
            textValue = message,
            onValueChange = { newValue -> message = newValue }
        )

        MyOutlinedTextField(
            textLabel = "Path",
            textValue = codePath,
            onValueChange = { newValue -> codePath = newValue }
        )

        MyOutlinedTextField(
            textLabel = "Method",
            textValue = codeMethod,
            onValueChange = { newValue -> codeMethod = newValue }
        )

        MyOutlinedTextField(
            textLabel = "Body?",
            textValue = codeBody,
            onValueChange = { newValue -> codeBody = newValue }
        )

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
}