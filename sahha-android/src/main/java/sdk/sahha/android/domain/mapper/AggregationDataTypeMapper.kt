package sdk.sahha.android.domain.mapper

class AggregationDataTypeMapper {
    fun appendDuration(type: String): String {
        val types = listOf(
            "sleep",
            "exercise"
        )

        return when (types.contains(type)) {
            true -> type + "_duration"
            false -> type
        }
    }
}