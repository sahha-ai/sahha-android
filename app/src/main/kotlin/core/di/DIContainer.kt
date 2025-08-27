package core.di

import kotlin.reflect.KClass

class DIContainer {
    private data class Key(val type: KClass<*>, val name: String?)
    private data class Provider(val singleton: Boolean, val create: (DIContainer) -> Any)

    private val instances = HashMap<Key, Any>()
    private val providers = HashMap<Key, Provider>()

    inline fun <reified T : Any> registerInstance(
        instance: T,
        name: String? = null,
        replace: Boolean = false
    ) = registerInstance(T::class, instance, name, replace)

    fun <T : Any> registerInstance(
        type: KClass<T>,
        instance: T,
        name: String? = null,
        replace: Boolean = false
    ) {
        val key = Key(type, name)
        synchronized(this) {
            if (!replace && (instances.containsKey(key) || providers.containsKey(key))) {
                throw IllegalStateException("Binding already exists for ${type.simpleName}${name?.let { "($it)" } ?: ""}")
            }
            instances[key] = instance
            providers.remove(key) // ensure instance wins
        }
    }

    inline fun <reified T : Any> registerSingleton(
        noinline provider: (DIContainer) -> T,
        name: String? = null,
        replace: Boolean = false
    ) = registerProvider(T::class, singleton = true, provider, name, replace)

    inline fun <reified T : Any> registerFactory(
        noinline provider: (DIContainer) -> T,
        name: String? = null,
        replace: Boolean = false
    ) = registerProvider(T::class, singleton = false, provider, name, replace)

    fun <T : Any> registerProvider(
        type: KClass<T>,
        singleton: Boolean,
        provider: (DIContainer) -> T,
        name: String? = null,
        replace: Boolean = false
    ) {
        val key = Key(type, name)
        synchronized(this) {
            if (!replace && (instances.containsKey(key) || providers.containsKey(key))) {
                throw IllegalStateException("Binding already exists for ${type.simpleName}${name?.let { "($it)" } ?: ""}")
            }
            providers[key] = Provider(singleton) { c -> provider(c) as Any }
            if (replace) instances.remove(key) // replacing provider should drop any eager/cached instance
        }
    }

    inline fun <reified T : Any> resolve(name: String? = null): T =
        resolve(T::class, name)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> resolve(type: KClass<T>, name: String? = null): T {
        val key = Key(type, name)
        synchronized(this) {
            instances[key]?.let { return it as T }

            val prov = providers[key]
                ?: throw IllegalStateException("No binding found for ${type.simpleName}${name?.let { "($it)" } ?: ""}")

            if (prov.singleton) {
                val created = prov.create(this) as T
                instances[key] = created as Any
                return created
            }

            return prov.create(this) as T
        }
    }

    fun clear() = synchronized(this) {
        instances.clear()
        providers.clear()
    }

    inline fun <reified T : Any> registerProviderFunc(
        noinline provider: (DIContainer) -> () -> T,
        name: String? = null,
        replace: Boolean = false
    ) {
        registerProvider(T::class, singleton = false, { c -> provider(c)() }, name, replace)  // Wrap to fit existing
    }

    inline fun <reified T : Any> resolveProvider(name: String? = null): () -> T = {
        resolve<T>(name)
    }
}