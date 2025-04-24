package sdk.sahha.android.domain.provider

import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.local_logs.SahhaSample
import sdk.sahha.android.domain.model.local_logs.SahhaStat
import sdk.sahha.android.source.SahhaSensor
import java.time.Duration
import java.time.ZonedDateTime

internal interface PermissionActionProvider {
    val permissionActionsStats: Map<SahhaSensor, suspend (Duration, ZonedDateTime, ZonedDateTime) -> Pair<String?, List<SahhaStat>?>>
    val permissionActionsSamples: Map<SahhaSensor, suspend (ZonedDateTime, ZonedDateTime) -> Pair<String?, List<SahhaSample>?>>
    val permissionActionsLogs: Map<SahhaSensor, suspend (Duration, ZonedDateTime, ZonedDateTime, String, String) -> Pair<String?, List<SahhaDataLog>?>>
}