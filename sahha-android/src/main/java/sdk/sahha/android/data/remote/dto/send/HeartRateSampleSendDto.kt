package sdk.sahha.android.data.remote.dto.send

data class HeartRateSampleSendDto(
    val timestamp: String,
    val beatsPerMinute: Long,
    val createdAt: String
)
