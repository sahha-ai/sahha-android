package sdk.sahha.android.domain.model.local_logs

import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethods

data class SahhaLogEvent(
    val id: String,
    val logType: String,
    val dataType: String,
    val value: Double,
    val source: String,
    val startDateTime: String,
    val endDateTime: String,
    val unit: String,
    val recordingMethod: String = RecordingMethods.UNKNOWN.name,
    val deviceType: String = Constants.UNKNOWN,
    val additionalProperties: HashMap<String, String>? = null,
    val parentId: String? = null,
)
