package sdk.sahha.android.framework.observer

import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.interaction.PermissionInteractionManager
import sdk.sahha.android.domain.internal_enum.AppEventEnum
import sdk.sahha.android.domain.model.app_event.AppEvent
import sdk.sahha.android.domain.use_case.background.LogAppEvent
import java.time.ZonedDateTime
import javax.inject.Inject

private const val TAG = "HostAppLifecycleObserver"

internal class HostAppLifecycleObserver @Inject constructor(
    private val context: Context,
    private val logAppEvent: LogAppEvent,
    private val permissionInteractionManager: PermissionInteractionManager,
    @IoScope private val scope: CoroutineScope,
) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        scope.launch {
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    val appEvent = AppEvent(
                        AppEventEnum.APP_CREATE.event,
                        ZonedDateTime.now(),
                    )

                    logAppEvent(event = appEvent)
                }

                Lifecycle.Event.ON_START -> {
                    val appEvent = AppEvent(
                        AppEventEnum.APP_START.event,
                        ZonedDateTime.now(),
                    )

                    logAppEvent(event = appEvent)
                    permissionInteractionManager.startHcOrNativeDataCollection(context) { error, successful ->
                        error?.also { e -> Log.d(TAG, e) }
                        if (successful) Log.d(TAG, "Restarted foreground service")
                    }
                }

                Lifecycle.Event.ON_RESUME -> {
                    val appEvent = AppEvent(
                        AppEventEnum.APP_RESUME.event,
                        ZonedDateTime.now(),
                    )

                    logAppEvent(event = appEvent)
                }

                Lifecycle.Event.ON_PAUSE -> {
                    val appEvent = AppEvent(
                        AppEventEnum.APP_PAUSE.event,
                        ZonedDateTime.now(),
                    )

                    logAppEvent(event = appEvent)
                }

                Lifecycle.Event.ON_STOP -> {
                    val appEvent = AppEvent(
                        AppEventEnum.APP_STOP.event,
                        ZonedDateTime.now(),
                    )

                    logAppEvent(event = appEvent)
                }

                Lifecycle.Event.ON_DESTROY -> {
                    val appEvent = AppEvent(
                        AppEventEnum.APP_DESTROY.event,
                        ZonedDateTime.now(),
                    )

                    logAppEvent(event = appEvent)
                }

                else -> {}
            }
        }
    }
}