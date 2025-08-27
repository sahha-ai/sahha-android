package core.events

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass

internal sealed interface Event



internal class SharedFlowEventBus(): EventBus {
    private val sharedFlow: MutableSharedFlow<Event> =
        MutableSharedFlow(
            replay = 0,
            extraBufferCapacity = 0,
            onBufferOverflow = BufferOverflow.SUSPEND
        )

    override suspend fun emit(event: Event) {
        sharedFlow.emit(event)
    }

    override fun <T : Event> observe(eventClass: KClass<T>): Flow<T> {
        return sharedFlow
            .filter { eventClass.isInstance(it) }
            .map { @Suppress("UNCHECKED_CAST") (it as T) }
    }
}