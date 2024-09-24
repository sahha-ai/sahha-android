package sdk.sahha.android.framework.observer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import javax.inject.Inject

internal class HostAppLifecycleObserver @Inject constructor(

) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> { println("ON_CREATE") }
            Lifecycle.Event.ON_START -> { println("ON_START") }
            Lifecycle.Event.ON_RESUME -> { println("ON_RESUME") }
            Lifecycle.Event.ON_PAUSE -> { println("ON_PAUSE") }
            Lifecycle.Event.ON_STOP -> { println("ON_STOP") }
            Lifecycle.Event.ON_DESTROY -> { println("ON_DESTROY") }
            Lifecycle.Event.ON_ANY -> { println("ON_ANY") }
        }
    }
}