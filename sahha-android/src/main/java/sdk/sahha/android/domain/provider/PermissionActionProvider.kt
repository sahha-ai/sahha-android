package sdk.sahha.android.domain.provider

import sdk.sahha.android.domain.model.local_logs.SahhaLogEvent
import sdk.sahha.android.domain.model.local_logs.SahhaStat
import sdk.sahha.android.source.SahhaSensor
import java.time.Duration
import java.time.ZonedDateTime

internal interface PermissionActionProvider {
    val permissionActionsStats: Map<SahhaSensor, suspend (Duration, ZonedDateTime, ZonedDateTime) -> Pair<String?, List<SahhaStat>?>>
    val permissionActionsEvents: Map<SahhaSensor, suspend (ZonedDateTime, ZonedDateTime) -> Pair<String?, List<SahhaLogEvent>?>>
}