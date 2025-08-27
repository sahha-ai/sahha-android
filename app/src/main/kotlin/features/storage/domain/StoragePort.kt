package features.storage.domain

interface StoragePort {
    suspend fun <T> get(key: StorageKey<T>): T?
    suspend fun <T> put(key: StorageKey<T>, value: T?)
    suspend fun remove(key: StorageKey<*>)
    suspend fun clear()
}