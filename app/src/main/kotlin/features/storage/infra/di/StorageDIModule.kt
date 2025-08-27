package features.storage.infra.di

import android.content.Context
import core.di.DIContainer
import core.di.DIModule
import core.di.STORAGE_PREFS
import core.di.STORAGE_SECURE
import features.storage.domain.StoragePort
import features.storage.infra.prefs.PrefsStoreAdapter
import features.storage.infra.secure.SecureStoreAdapter
import kotlinx.serialization.json.Json

object StorageDIModule : DIModule {
    override fun register(container: DIContainer) {
        container.registerSingleton<StoragePort>(
            provider = { c ->
                SecureStoreAdapter(
                    appContext = c.resolve<Context>(),
                    json = c.resolve<Json>()
                )
            },
            name = STORAGE_SECURE
        )

        container.registerSingleton<StoragePort>(
            provider = { c ->
                PrefsStoreAdapter(
                    appContext = c.resolve<Context>(),
                    json = c.resolve<Json>()
                )
            },
            name = STORAGE_PREFS
        )

    }
}