package empty.sahha.android.views

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import empty.sahha.android.ui.theme.SahhasdkemptyTheme
import java.time.ZonedDateTime

val gson = GsonBuilder()
    .registerTypeAdapter(
        ZonedDateTime::class.java,
        JsonSerializer<ZonedDateTime> { src, _, _ ->
            JsonPrimitive(src.toString())
        }
    )
    .setPrettyPrinting()
    .create()

@Composable
fun MyOutlinedTextField(
    textLabel: String,
    textValue: String,
    imeAction: ImeAction = ImeAction.Done,
    lfm: FocusManager = LocalFocusManager.current,
    onValueChange: (newValue: String) -> Unit
) {
    OutlinedTextField(
        value = textValue,
        onValueChange = { onValueChange(it) },
        label = { Text(textLabel) },
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { lfm.clearFocus() })
    )
}
