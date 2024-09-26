package sdk.sahha.android.domain.use_case.background

import kotlinx.coroutines.runBlocking
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.AppEventEnum
import sdk.sahha.android.domain.model.app_event.AppEvent
import sdk.sahha.android.domain.model.health_connect.HealthConnectQuery
import sdk.sahha.android.domain.repository.HealthConnectRepo
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

internal class LogAppAliveState @Inject constructor(
    private val logAppEvent: LogAppEvent,
    private val repo: HealthConnectRepo,
) {
    suspend operator fun invoke(
        lastAppAliveQuery: HealthConnectQuery? = runBlocking {
            repo.getLastCustomQuery(Constants.APP_ALIVE_QUERY_ID)
        }
    ): Boolean {
        return lastAppAliveQuery?.let { lastQuery ->
            val instant = Instant.ofEpochMilli(lastQuery.lastSuccessfulTimeStampEpochMillis)
            val day = instant.truncatedTo(ChronoUnit.DAYS)
            val today = Instant.now().truncatedTo(ChronoUnit.DAYS)

            val queryCompletedToday = day == today

            if (queryCompletedToday) return true

            logAppAliveEvent()
        } ?: logAppAliveEvent()
    }

    private suspend fun logAppAliveEvent(): Boolean {
        val now = ZonedDateTime.now()
        logAppEvent(
            AppEvent(
                AppEventEnum.APP_ALIVE.event,
                now
            )
        )
        repo.saveCustomSuccessfulQuery(
            Constants.APP_ALIVE_QUERY_ID,
            now
        )
        return false
    }
}