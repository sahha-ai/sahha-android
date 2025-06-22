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
import sdk.sahha.android.source.SahhaDemographic
import java.time.LocalDate
import kotlin.random.Random

@Composable
fun DemographicView() {
    var postDemoStatus by remember { mutableStateOf("") }
    var getDemoStatus by remember { mutableStateOf("") }

    Column {
        // Post Demographic Section
        Button(onClick = {
            val rnd = Random.Default
            val gendersList = listOf("Male", "Female", "Gender diverse")
            val incomeRangeList = listOf(
                "Up to $15,000", "Up to $25,000", "Up to $50,000",
                "Up to $75,000", "Up to $100,000", "Up to $125,000",
                "Up to $150,000", "Up to $175,000", "Up to and over $200,000"
            )
            val educationList = listOf(
                "Primary", "Secondary", "Tertiary",
                "Masters", "Doctoral", "Trade"
            )
            val relationshipList = listOf("Single", "Partner", "Spouse")
            val localeList = listOf("Rural", "Urban")
            val livingArrangementList = listOf("Renting", "Home owner", "Homeless")
            val birthDate = rnd.nextInt(1900, 2016)

            postDemoStatus = ""

            Sahha.postDemographic(
                SahhaDemographic(
                    age = LocalDate.now().year - birthDate,
                    gender = gendersList[rnd.nextInt(gendersList.size)],
                    incomeRange = incomeRangeList[rnd.nextInt(incomeRangeList.size)],
                    education = educationList[rnd.nextInt(educationList.size)],
                    relationship = relationshipList[rnd.nextInt(relationshipList.size)],
                    locale = localeList[rnd.nextInt(localeList.size)],
                    livingArrangement = livingArrangementList[rnd.nextInt(livingArrangementList.size)],
                    birthDate = "${birthDate}-01-01"
                )
            ) { error, success ->
                postDemoStatus = if (success) "Successful" else error ?: "Failed"
            }
        }) {
            Text("Post Demographic")
        }

        if (postDemoStatus.isNotEmpty()) {
            Text(postDemoStatus)
        }

        Spacer(modifier = Modifier.padding(16.dp))

        // Get Demographic Section
        Button(onClick = {
            Sahha.getDemographic { error, demographic ->
                error?.also { getDemoStatus = it }
                demographic?.also {
                    getDemoStatus = buildString {
                        append("${it.age}, ")
                        append("${it.gender}, ")
                        append("${it.country}, ")
                        append("${it.birthCountry}, ")
                        append("${it.ethnicity}, ")
                        append("${it.occupation}, ")
                        append("${it.industry}, ")
                        append("${it.incomeRange}, ")
                        append("${it.education}, ")
                        append("${it.relationship}, ")
                        append("${it.locale}, ")
                        append("${it.livingArrangement}, ")
                        append(it.birthDate)
                    }
                }
            }
        }) {
            Text("Get Demographic")
        }

        if (getDemoStatus.isNotEmpty()) {
            Text(getDemoStatus)
        }
    }
}