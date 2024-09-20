package sdk.sahha.android.framework.runnable

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.common.Session
import sdk.sahha.android.di.DefaultScope
import sdk.sahha.android.domain.interaction.SahhaInteractionManager
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.model.config.toSahhaSensorSet
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus
import java.time.ZonedDateTime
import javax.inject.Inject

private const val TAG = "DataCollectionPeriodicTask"
private const val LOOP_INTERVAL = 15 * 60 * 1000L

internal class DataCollectionPeriodicTask @Inject constructor(
    private val context: Context,
    private val permissionManager: PermissionManager,
    private val sahhaInteractionManager: SahhaInteractionManager,
    private val configRepo: SahhaConfigRepo,
    @DefaultScope private val scope: CoroutineScope,
) : Runnable {
    override fun run() {
        scope.launch {
            permissionManager.getHealthConnectSensorStatus(
                context = context,
                sensors = getSensorSet()
            ) { _, status ->
                val isEnabledOrDisabled =
                    status == SahhaSensorStatus.enabled || status == SahhaSensorStatus.disabled
                if (!isEnabledOrDisabled) Session.handlerThread.quitSafely()
            }

            if (Session.handlerRunning) {
                Log.d(TAG, "Handler is running, exiting task")
                return@launch
            }

            try {
                Session.handlerRunning = true
                Log.d(
                    TAG, "Handler task started at ${ZonedDateTime.now().toLocalTime()}\n" +
                            "Thread: ${Session.handlerThread.threadId}\n\n"
                )

                queryHealthConnect { _, _ ->
                    Session.handlerRunning = false
                }
                Session.serviceHandler.postDelayed(this@DataCollectionPeriodicTask, LOOP_INTERVAL)
            } catch (e: Exception) {
                Session.handlerRunning = false
                Log.e(TAG, "Periodic task failed", e)
            }
        }
    }

    private suspend fun getSensorSet(): Set<SahhaSensor> {
        return configRepo.getConfig().sensorArray.toSahhaSensorSet()
    }

    private suspend fun queryHealthConnect(
        onComplete: ((error: String?, successful: Boolean) -> Unit)? = null
    ) {
        sahhaInteractionManager
            .sensor
            .queryWithMinimumDelay(
                afterTimer = {}
            ) { error, successful ->
                Session.healthConnectPostCallback?.invoke(error, successful)
                Session.healthConnectPostCallback = null

                onComplete?.invoke(error, successful)

                error?.also { e ->
                    Sahha.di.sahhaErrorLogger.application(
                        e, TAG, "queryHealthConnect"
                    )
                }
            }
    }
}