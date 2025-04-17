package sdk.sahha.android.domain.model.dto

import sdk.sahha.android.domain.model.health_connect.HealthConnectQuery

internal data class QueryTime(
    val id: String,
    val timeEpochMilli: Long,
)

internal fun HealthConnectQuery.toQueryTime(): QueryTime {
    return QueryTime(
        id = id,
        timeEpochMilli = lastSuccessfulTimeStampEpochMillis
    )
}

internal fun QueryTime.toHealthConnectQuery(): HealthConnectQuery {
    return HealthConnectQuery(
        id = id,
        lastSuccessfulTimeStampEpochMillis = timeEpochMilli
    )
}
