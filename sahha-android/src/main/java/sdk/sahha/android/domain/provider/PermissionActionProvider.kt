package sdk.sahha.android.domain.provider

import sdk.sahha.android.domain.model.stats.SahhaStat
import sdk.sahha.android.source.SahhaSensor
import java.time.Duration
import java.time.ZonedDateTime

internal interface PermissionActionProvider {
    val permissionActions: Map<SahhaSensor, suspend (Duration, ZonedDateTime, ZonedDateTime) -> Pair<String?, List<SahhaStat>?>>
}