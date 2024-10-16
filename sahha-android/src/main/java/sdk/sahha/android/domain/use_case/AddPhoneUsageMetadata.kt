package sdk.sahha.android.domain.use_case

import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.model.data_log.SahhaMetadata
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.repository.DeviceUsageRepo
import javax.inject.Inject

internal class AddPhoneUsageMetadata @Inject constructor(
    private val timeManager: SahhaTimeManager,
    private val repo: DeviceUsageRepo,
) {
    suspend operator fun invoke(
        phoneUsages: List<PhoneUsage>,
        postDateTime: String = timeManager.nowInISO()
    ): List<PhoneUsage> {
        val modifiedData = mutableListOf<PhoneUsage>()

        phoneUsages.forEach { usage ->
            val metadata = usage.metadata
            val postDateTimes = metadata?.postDateTime?.toMutableList()

            modifiedData += postDateTimes?.let { times ->
                appendPostDateTime(usage.copy(metadata = SahhaMetadata(times)), postDateTime)
            } ?: appendPostDateTime(usage, postDateTime)
        }

        repo.saveUsages(modifiedData)
        return modifiedData
    }

    private fun appendPostDateTime(dataLog: PhoneUsage, postDateTime: String): PhoneUsage {
        val postDateTimes = dataLog.metadata?.postDateTime?.toMutableList() ?: mutableListOf()
        postDateTimes.add(postDateTime)
        return dataLog.copy(metadata = SahhaMetadata(postDateTimes))
    }
}