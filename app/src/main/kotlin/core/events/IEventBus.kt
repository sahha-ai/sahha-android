package core.events

import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

internal interface EventBus {
    suspend fun emit(event: Event)
    fun<T: Event>observe(eventClass: KClass<T>): Flow<T>
}