package sdk.sahha.android.domain.use_case.background

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.repository.BatchedDataRepo
import javax.inject.Inject

internal class FilterActivityOverlaps @Inject constructor(
    private val repo: BatchedDataRepo,
    private val timeManager: SahhaTimeManager
) {
    suspend operator fun invoke(
        data: List<SahhaDataLog> = runBlocking { repo.getBatchedData() }
    ): List<SahhaDataLog> {
        return filterOverlaps(data)
    }

    private suspend fun filterOverlaps(data: List<SahhaDataLog>): List<SahhaDataLog> =
        coroutineScope {
            val dataBySource = data.groupBy { it.source }
            val deferredResults = dataBySource.map { (_, sourceData) ->
                async(Dispatchers.Default) {
                    val sortedData =
                        sourceData.sortedWith(compareBy({ it.startDateTime }, { it.endDateTime }))

                    val ignoredLogs = mutableListOf<SahhaDataLog>()
                    val sourceFiltered = mutableListOf<SahhaDataLog>()
                    for (currentInterval in sortedData) {
                        if(currentInterval.dataType != Constants.DataTypes.STEP) {
                            ignoredLogs.add(currentInterval)
                            continue
                        }

                        if(sourceFiltered.isEmpty()) {
                            sourceFiltered.add(currentInterval)
                            continue
                        }

                        val lastEndTime = sourceFiltered.last().endDateTime
                        val lastEndTimeConverted = timeManager.ISOToZonedDateTime(lastEndTime)
                        val lastEndEpochSeconds = lastEndTimeConverted.toEpochSecond()
                        val currentStartTime = currentInterval.startDateTime
                        val currentStartTimeConverted = timeManager.ISOToZonedDateTime(currentStartTime)
                        val currentStartEpochSeconds = currentStartTimeConverted.toEpochSecond()
                        val currentEndTime = currentInterval.endDateTime
                        val currentEndTimeConverted = timeManager.ISOToZonedDateTime(currentEndTime)
                        val currentEndEpochSeconds = currentEndTimeConverted.toEpochSecond()

                        val isNonOverlapping = lastEndEpochSeconds <= currentStartEpochSeconds
                                && lastEndEpochSeconds < currentEndEpochSeconds
                        val isLongerInterval =
                            !isNonOverlapping && currentEndEpochSeconds >= lastEndEpochSeconds

                        when {
                            isNonOverlapping -> sourceFiltered.add(currentInterval)
                            isLongerInterval -> sourceFiltered[sourceFiltered.lastIndex] =
                                currentInterval
                        }
                    }
                    sourceFiltered + ignoredLogs
                }
            }

            deferredResults.awaitAll().flatten()
        }
}