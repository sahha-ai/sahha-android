package application.di

import application.services.AuthService
import core.di.DIContainer
import core.di.DIModule
import core.json.SdkJson
import features.auth.domain.AuthPort
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

object ApplicationDIModule : DIModule {
    override fun register(container: DIContainer) {

        container.registerProviderFunc<AuthService>(
            provider = { c ->
                {
                    AuthService(
                        authPort = c.resolve<AuthPort>(),
                        ioDispatcher = Dispatchers.IO
                    )
                }
            }
        )
    }
}