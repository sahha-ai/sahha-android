package features.auth.infra.di

import core.di.DIContainer
import core.di.DIModule
import core.di.STORAGE_SECURE
import features.auth.domain.AuthPort
import features.auth.infra.AuthApi
import features.auth.infra.AuthAdapter
import features.auth.infra.AuthApiImpl
import features.auth.infra.AuthStore
import features.server.domain.JsonHttpPort

object AuthDIModule : DIModule {
    override fun register(container: DIContainer) {
        container.registerSingleton<AuthStore>(
            provider = { c ->
                AuthStore(c.resolve<features.storage.domain.StoragePort>(name = STORAGE_SECURE))
            }
        )

        container.registerSingleton<AuthApi>(
            provider = { c ->
                AuthApiImpl(c.resolveProvider<JsonHttpPort>())
            }
        )

        container.registerProviderFunc<AuthPort>(
            provider = { c ->
                {
                    AuthAdapter(
                        api = c.resolve<AuthApi>(),
                        store = c.resolve<AuthStore>()
                    )
                }
            }
        )
    }
}