package sdk.sahha.android.domain.use_case.metadata

import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.model.data_log.SahhaMetadata
import sdk.sahha.android.domain.model.steps.StepSession
import sdk.sahha.android.domain.repository.SensorRepo
import javax.inject.Inject

internal class AddStepSessionMetadata @Inject constructor(
    private val timeManager: SahhaTimeManager,
    private val repo: SensorRepo
) {
    suspend operator fun invoke(
        stepSessions: List<StepSession>,
        postDateTime: String = timeManager.nowInISO()
    ): List<StepSession> {
        val modifiedData = mutableListOf<StepSession>()

        stepSessions.forEach { session ->
            val metadata = session.metadata
            val postDateTimes = metadata?.postDateTime?.toMutableList()

            modifiedData += postDateTimes?.let { times ->
                appendPostDateTime(session.copy(metadata = SahhaMetadata(times)), postDateTime)
            } ?: appendPostDateTime(session, postDateTime)
        }

        modifiedData.forEach { session ->
            repo.saveStepSession(session)
        }
        return modifiedData
    }

    private fun appendPostDateTime(dataLog: StepSession, postDateTime: String): StepSession {
        val postDateTimes = dataLog.metadata?.postDateTime?.toMutableList() ?: mutableListOf()
        postDateTimes.add(postDateTime)
        return dataLog.copy(metadata = SahhaMetadata(postDateTimes))
    }
}