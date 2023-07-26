package sdk.sahha.android.domain.model.worker_action

import kotlin.reflect.KFunction2

data class SahhaWorkerAction(
    val workerTag: String,
    val workerStarter: KFunction2<Long, String, Unit>,
    val repeatInterval: Long
)
