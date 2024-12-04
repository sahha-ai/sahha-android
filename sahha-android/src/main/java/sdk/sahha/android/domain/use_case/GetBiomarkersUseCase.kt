package sdk.sahha.android.domain.use_case

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.di.DefaultScope
import sdk.sahha.android.domain.repository.UserDataRepo
import sdk.sahha.android.source.SahhaBiomarkerCategory
import sdk.sahha.android.source.SahhaBiomarkerType
import java.time.LocalDateTime
import java.util.Date
import javax.inject.Inject

internal class GetBiomarkersUseCase @Inject constructor(
    private val repository: UserDataRepo,
    @DefaultScope private val defaultScope: CoroutineScope,
    private val timeManager: SahhaTimeManager,
) {
    operator fun invoke(
        categories: Set<SahhaBiomarkerCategory>,
        types: Set<SahhaBiomarkerType>,
        dates: Pair<Date, Date>? = null,
        localDates: Pair<LocalDateTime, LocalDateTime>? = null,
        callback: ((error: String?, value: String?) -> Unit)
    ) {
        defaultScope.launch {
            repository.getBiomarkers(
                categories.map { it.name },
                types.map { it.name },
                localDates?.let {
                    Pair(
                        timeManager.localDateTimeToISO(it.first),
                        timeManager.localDateTimeToISO(it.second)
                    )
                } ?: dates?.let {
                    Pair(
                        timeManager.dateToISO(it.first),
                        timeManager.dateToISO(it.second)
                    )
                },
                callback
            )
        }
    }
}