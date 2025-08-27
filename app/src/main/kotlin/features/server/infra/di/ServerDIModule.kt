package features.server.infra.di

import core.config.SahhaEnvironment  // Import the new enum
import core.di.DIContainer
import core.di.DIModule
import features.auth.domain.AuthPort
import features.server.domain.HttpPort
import features.server.domain.JsonHttpPort
import features.server.infra.interceptors.BearerAuthInterceptor
import features.server.infra.http.HttpJsonClientAdapter
import features.server.infra.http.NetworkConfig
import features.server.infra.retry.ExponentialBackoffPolicy
import features.server.infra.retry.RetryPolicy
import kotlinx.serialization.json.Json

class ServerDIModule(private val environment: SahhaEnvironment) : DIModule {
    override fun register(container: DIContainer) {

        // Register network config based on environment (customize timeouts if env-specific)
        container.registerInstance<NetworkConfig>(
            NetworkConfig(
                baseUrl = environment.baseUrl,
                connectTimeoutMillis = 10_000,
                readTimeoutMillis = 15_000
            )
        )

        // Register retry policy using ExponentialBackoffPolicy (customize params if needed)
        container.registerInstance<RetryPolicy>(
            ExponentialBackoffPolicy(
                baseMs = 500,      // Initial delay
                maxMs = 30_000,    // Cap delay at 30s
                maxAttempts = 6,   // Up to 6 retries (7 total attempts)
                jitterPct = 0.2    // 20% jitter for anti-thundering herd
            )
        )

        container.registerSingleton<BearerAuthInterceptor>(
            provider = { c ->
                BearerAuthInterceptor(c.resolveProvider<AuthPort>())
            }
        )

        container.registerSingleton<HttpPort>(
            provider = { c ->
                HttpJsonClientAdapter(  // Renamed; use HttpJsonClientAdapter if keeping old name
                    cfg = c.resolve<NetworkConfig>(),
                    policy = c.resolve<RetryPolicy>(),
                    interceptors = listOf(c.resolve<BearerAuthInterceptor>())
                )
            }
        )

        container.registerSingleton<JsonHttpPort>(
            provider = { c ->
                JsonHttpPort(
                    http = c.resolve<HttpPort>(),
                    json = c.resolve<Json>()
                )
            }
        )
    }
}