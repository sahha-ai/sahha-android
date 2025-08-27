package app

import android.content.Context
import application.di.ApplicationDIModule
import core.config.SahhaEnvironment
import core.config.SahhaSettings
import core.di.CoreDIModule
import core.di.DIContainer
import features.auth.infra.di.AuthDIModule
import features.server.infra.di.ServerDIModule
import features.storage.infra.di.StorageDIModule  // Assuming this exists; import accordingly
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object Sahha {
    private lateinit var diContainer: DIContainer
    private lateinit var appContext: Context

    /**
     * Configures the SDK with the provided settings.
     * This sets up the dependency injection container, registers modules based on the environment,
     * and handles any initial configuration. The callback is invoked with an error message if setup fails,
     * or success as true if it completes successfully.
     */
    fun configure(
        context: Context,
        settings: SahhaSettings,
        callback: (error: String?, success: Boolean) -> Unit
    ) {
        try {
            appContext = context.applicationContext
            diContainer = DIContainer()

            diContainer.registerInstance<Context>(appContext)
            listOf(
                CoreDIModule,
                ServerDIModule(settings.environment),
                AuthDIModule,
                StorageDIModule,
                ApplicationDIModule
            ).forEach { it.register(diContainer) }

            // Optional: Register notification settings if used in other features
            // For example, if you have a ConfigDIModule:
            // ConfigDIModule(settings).register(diContainer)

            // If there are async operations (e.g., permission checks), launch a coroutine here
            // For now, assume synchronous success
            callback(null, true)
        } catch (e: Exception) {
            callback(e.message ?: "Unknown error during configuration", false)
        }
    }

    // Additional functions can be added here, e.g., for authentication
    /**
     * Authenticates the SDK using app credentials.
     * This should be called after configure if API interactions are needed.
     * It registers a profile and persists the token.
     */
    fun authenticate(
        appId: String,
        appSecret: String,
        externalId: String? = null,
        callback: (error: String?, success: Boolean) -> Unit
    ) {
        GlobalScope.launch(Dispatchers.IO) {  // Use IO dispatcher for network/storage ops
            try {
                val authService = diContainer.resolve<application.services.AuthService>()
                val result = authService.register(appId, appSecret, externalId)
                if (result.isSuccess) {
                    callback(null, true)
                } else {
                    callback(result.exceptionOrNull()?.message ?: "Authentication failed", false)
                }
            } catch (e: Exception) {
                callback(e.message ?: "Unknown error during authentication", false)
            }
        }
    }

    // Other SDK functions, e.g., getService<T>(), startSensors(), etc.
}