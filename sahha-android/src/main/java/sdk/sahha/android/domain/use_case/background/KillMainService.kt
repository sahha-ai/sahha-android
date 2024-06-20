package sdk.sahha.android.domain.use_case.background

import android.content.Context
import sdk.sahha.android.source.Sahha
import javax.inject.Inject

internal class KillMainService @Inject constructor(
    private val context: Context,
) {
    operator fun invoke() {
        Sahha.di.sahhaInteractionManager.sensor.killMainService(context = context)
    }
}