package sdk.sahha.android.presentation.usage_stats

import android.graphics.drawable.Drawable
import android.view.Gravity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AppShortcut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import sdk.sahha.android.presentation.usage_stats.components.AppUsageSettingListing
import sdk.sahha.android.presentation.usage_stats.components.AppUsageSettingPermissionScreen

@Preview
@Composable
internal fun AppUsagePromptPreview() {
    AppUsagePrompt(
        appName = "ExampleApp",
        visible = true,
        onSettings = {},
        onDismiss = {}
    )
}

@Composable
internal fun AppUsagePrompt(
    appName: String,
    appIcon: Drawable? = null,
    visible: Boolean = false,
    fontSize: Float = 16f,
    onSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            val dialogProvider = LocalView.current.parent as DialogWindowProvider
            dialogProvider.window.setGravity(Gravity.BOTTOM)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier
                        .wrapContentSize()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(topStartPercent = 8, topEndPercent = 8),
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(
                            top = 20.dp,
                            start = 20.dp,
                            end = 20.dp,
                            bottom = 10.dp,
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        item {
                            Icon(
                                imageVector = Icons.Rounded.AppShortcut,
                                contentDescription = "Picture of a phone",
                                modifier = Modifier
                                    .padding(20.dp)
                                    .size(50.dp)
                            )
                        }
                        item {
                            Text(
                                text = "To deliver enhanced health scores, $appName requires access to your phone's usage data.",
                                textAlign = TextAlign.Center,
                                lineHeight = TextUnit(1.5f, TextUnitType.Em),
                                fontSize = TextUnit(fontSize, TextUnitType.Sp),
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            Text(
                                text = "Your personal information and in-app activities remain private and untracked.",
                                textAlign = TextAlign.Center,
                                lineHeight = TextUnit(1.5f, TextUnitType.Em),
                                fontSize = TextUnit(fontSize, TextUnitType.Sp),
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append(
                                        "For permission:\n" +
                                                "\t\t1. Tap "
                                    )
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Settings")
                                    }
                                },
                                textAlign = TextAlign.Start,
                                lineHeight = TextUnit(1.5f, TextUnitType.Em),
                                fontSize = TextUnit(fontSize, TextUnitType.Sp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append(
                                        "\t\t2. Find "
                                    )
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(appName)
                                    }
                                    append(
                                        " in the list"
                                    )

                                },
                                textAlign = TextAlign.Start,
                                lineHeight = TextUnit(1.5f, TextUnitType.Em),
                                fontSize = TextUnit(fontSize, TextUnitType.Sp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            )
                            AppUsageSettingListing(appName = appName, appIcon = appIcon)
                            Spacer(modifier = Modifier.size(10.dp))
                            Text(
                                text = buildAnnotatedString {
                                    append("\t\t3. Toggle on ")
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Permit usage access")
                                    }
                                },
                                textAlign = TextAlign.Start,
                                lineHeight = TextUnit(1.5f, TextUnitType.Em),
                                fontSize = TextUnit(fontSize, TextUnitType.Sp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            )
                            AppUsageSettingPermissionScreen(
                                appName = appName,
                                appIcon = appIcon
                            )
                            Spacer(modifier = Modifier.size(10.dp))
                        }
                        item {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                Button(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 5.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = Color.Transparent,
                                        contentColor = MaterialTheme.colors.error
                                    ),
                                    elevation = ButtonDefaults.elevation(
                                        defaultElevation = 0.dp
                                    ),
                                    onClick = onDismiss
                                ) {
                                    Text(
                                        "Cancel",
                                        fontSize = TextUnit(fontSize, TextUnitType.Sp)
                                    )
                                }
                                Button(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 5.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = Color.Transparent,
                                        contentColor = MaterialTheme.colors.primary
                                    ),
                                    elevation = ButtonDefaults.elevation(
                                        defaultElevation = 0.dp
                                    ),
                                    onClick = { onSettings() }
                                ) {
                                    Text(
                                        text = "Settings",
                                        fontSize = TextUnit(fontSize, TextUnitType.Sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}