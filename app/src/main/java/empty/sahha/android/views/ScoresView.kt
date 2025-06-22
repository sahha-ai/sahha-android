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
import sdk.sahha.android.source.SahhaScoreType
import java.time.LocalDateTime
import java.util.*

const val SEVEN_DAYS_MILLIS = 604800000L

@Composable
fun ScoresView() {
    var scoresResponse by remember { mutableStateOf("") }
    var scoresResponseDate by remember { mutableStateOf("") }
    var scoresResponseLocalDateTime by remember { mutableStateOf("") }

    Column {
        Button(onClick = {
            scoresResponse = ""
            scoresResponseDate = ""
            scoresResponseLocalDateTime = ""

            val now = Date()
            val lastWeek = Date(now.time - SEVEN_DAYS_MILLIS)

            // Get scores without date range
            Sahha.getScores(
                setOf(SahhaScoreType.activity, SahhaScoreType.sleep)
            ) { error, success ->
                error?.also { scoresResponse = it }
                success?.also { scoresResponse = it }
            }

            // Get scores with Date range
            Sahha.getScores(
                types = setOf(SahhaScoreType.activity, SahhaScoreType.sleep),
                dates = Pair(lastWeek, now),
            ) { error, success ->
                error?.also { scoresResponseDate = it }
                success?.also { scoresResponseDate = it }
            }

            // Get scores with LocalDateTime range
            Sahha.getScores(
                types = setOf(SahhaScoreType.activity, SahhaScoreType.sleep),
                dates = Pair(LocalDateTime.now(), LocalDateTime.now()),
            ) { error, success ->
                error?.also { scoresResponseLocalDateTime = it }
                success?.also { scoresResponseLocalDateTime = it }
            }
        }) {
            Text("Get Scores")
        }

        if (scoresResponse.isNotEmpty()) {
            Text(scoresResponse)
        }
        if (scoresResponseDate.isNotEmpty()) {
            Text(scoresResponseDate)
        }
        if (scoresResponseLocalDateTime.isNotEmpty()) {
            Text(scoresResponseLocalDateTime)
        }
    }
}