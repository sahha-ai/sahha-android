package sdk.sahha.android.domain.use_case.metadata

import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.model.metadata.HasMetadata
import sdk.sahha.android.domain.model.metadata.SahhaMetadata
import javax.inject.Inject

internal class AddMetadata @Inject constructor(
    private val timeManager: SahhaTimeManager
) {
    suspend operator fun <T : HasMetadata<T>> invoke(
        dataList: List<T>,
        saveData: suspend (List<T>) -> Unit,
        postDateTime: String = timeManager.nowInISO()
    ): List<T> {
        val modifiedData = dataList.map { item ->
            val postDateTimes = item.metadata?.postDateTime?.toMutableList() ?: mutableListOf()
            postDateTimes.add(postDateTime)
            val newMetadata = SahhaMetadata(postDateTimes)
            item.copyWithMetadata(newMetadata)
        }
        saveData(modifiedData)
        return modifiedData
    }
}