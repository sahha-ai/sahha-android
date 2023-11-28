package sdk.sahha.android.domain.model.health_connect

import androidx.health.connect.client.records.Record
import kotlin.reflect.KClass

data class HealthConnectPostParameters<T: Record>(
    val error: String?,
    val successful: Boolean,
    val successfulLog: String,
    val records: List<T>,
    val recordClass: KClass<T>,
)
