package features.storage.domain

import kotlinx.serialization.KSerializer

/** Typed key for strongly-typed storage operations. */
data class StorageKey<T>(val name: String, val serializer: KSerializer<T>)