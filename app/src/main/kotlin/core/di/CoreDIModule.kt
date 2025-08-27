package core.di

import kotlinx.serialization.json.Json

object CoreDIModule : DIModule {
    override fun register(container: DIContainer) {
        container.registerInstance<Json>(
            Json {
                ignoreUnknownKeys = true  // Keep the same configuration as before
                // Add other customizations if needed, e.g., isLenient = true
            }
        )
    }
}