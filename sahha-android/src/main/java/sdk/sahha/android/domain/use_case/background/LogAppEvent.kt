package sdk.sahha.android.domain.use_case.background

import android.content.Context
import sdk.sahha.android.domain.mapper.HealthConnectConstantsMapper
import sdk.sahha.android.domain.model.app_event.AppEvent
import sdk.sahha.android.domain.model.app_event.toSahhaDataLog
import sdk.sahha.android.domain.repository.BatchedDataRepo
import sdk.sahha.android.source.SahhaConverterUtility
import javax.inject.Inject

internal class LogAppEvent @Inject constructor(
    private val context: Context,
    private val repo: BatchedDataRepo,
    private val mapper: HealthConnectConstantsMapper
) {
    suspend operator fun invoke(event: AppEvent) {
        val appEvent = event.toSahhaDataLog(
            context = context,
            mapper = mapper
        )
        repo.saveBatchedData(listOf(appEvent))
    }
}