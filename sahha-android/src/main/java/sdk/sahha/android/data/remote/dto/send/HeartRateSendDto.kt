package sdk.sahha.android.data.remote.dto.send

import androidx.annotation.Keep

@Keep
data class HeartRateSendDto(
    val startDateTime: String,
    val endDateTime: String,
    val samples: List<HeartRateSampleSendDto>
)
