package sdk.sahha.android.framework.observer

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.internal_enum.AppEventEnum
import sdk.sahha.android.domain.model.app_event.AppEvent
import sdk.sahha.android.domain.model.processor.AppEventProcessor
import sdk.sahha.android.domain.repository.BatchedDataRepo
import java.time.ZonedDateTime
import javax.inject.Inject

private const val TAG = "HostAppLifecycleObserver"

internal class HostAppLifecycleObserver @Inject constructor(
    private val processor: AppEventProcessor,
    private val repository: BatchedDataRepo,
    @IoScope private val scope: CoroutineScope,
) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        scope.launch {
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    val appEvent = AppEvent(
                        AppEventEnum.APP_CREATE,
                        ZonedDateTime.now(),
                    )

                    processor.process(appEvent)
                }

                Lifecycle.Event.ON_START -> {
                    val appEvent = AppEvent(
                        AppEventEnum.APP_START,
                        ZonedDateTime.now(),
                    )

                    processor.process(appEvent)
                }

                Lifecycle.Event.ON_RESUME -> {
                    val appEvent = AppEvent(
                        AppEventEnum.APP_RESUME,
                        ZonedDateTime.now(),
                    )

                    processor.process(appEvent)
                }

                Lifecycle.Event.ON_PAUSE -> {
                    val appEvent = AppEvent(
                        AppEventEnum.APP_PAUSE,
                        ZonedDateTime.now(),
                    )

                    val log = processor.process(appEvent)
                    log?.also { repository.saveBatchedData(listOf(it)) }
                }

                Lifecycle.Event.ON_STOP -> {
                    val appEvent = AppEvent(
                        AppEventEnum.APP_STOP,
                        ZonedDateTime.now(),
                    )

                    val log = processor.process(appEvent)
                    log?.also { repository.saveBatchedData(listOf(it)) }
                }

                Lifecycle.Event.ON_DESTROY -> {
                    val appEvent = AppEvent(
                        AppEventEnum.APP_DESTROY,
                        ZonedDateTime.now(),
                    )

                    val log = processor.process(appEvent)
                    log?.also {
                        Log.d(TAG, "DESTROY event log found")
                        repository.saveBatchedData(listOf(it))
                    }
                }

                else -> {}
            }
        }
    }
}