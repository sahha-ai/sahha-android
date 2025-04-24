package sdk.sahha.android.domain.transformer

import sdk.sahha.android.domain.mapper.AggregationDataTypeMapper
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.dto.send.SahhaDataLogDto
import sdk.sahha.android.domain.model.dto.send.toSahhaDataLogDto
import javax.inject.Inject

internal class AggregateDataLogTransformer @Inject constructor(
    private val dataTypeMapper: AggregationDataTypeMapper,
) {
    fun transformDataLog(log: SahhaDataLog): SahhaDataLogDto {
        return log.additionalProperties?.get("aggregation")?.let { aggr ->
            val aggregationIsDay = (aggr as String) == "day"

            if (aggregationIsDay)
                log.toSahhaDataLogDto(dataTypeMapper.appendDuration(log.dataType))
            else log.toSahhaDataLogDto()
        } ?: log.toSahhaDataLogDto()
    }
}