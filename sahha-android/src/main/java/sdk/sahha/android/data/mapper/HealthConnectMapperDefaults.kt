package sdk.sahha.android.data.mapper

import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.manager.IdManager
import sdk.sahha.android.domain.mapper.HealthConnectConstantsMapper
import javax.inject.Inject

internal class HealthConnectMapperDefaults @Inject constructor(
    val mapper: HealthConnectConstantsMapper,
    val timeManager: SahhaTimeManager,
    val idManager: IdManager
)