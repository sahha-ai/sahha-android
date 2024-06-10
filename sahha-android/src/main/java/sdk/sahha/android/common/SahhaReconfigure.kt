package sdk.sahha.android.common

import android.content.Context
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import sdk.sahha.android.data.local.SahhaDbUtility
import sdk.sahha.android.di.AppComponent
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.di.DaggerAppComponent
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.model.config.toSahhaConfiguration
import sdk.sahha.android.domain.model.config.toSahhaSettings
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSettings

internal object SahhaReconfigure {
    suspend operator fun invoke(
        context: Context,
        environment: SahhaEnvironment? = null
    ) {
        withContext(Main) {
            val settings = environment?.let { env ->
                val currentSettings = getSahhaSettings(context)
                val newSettings = SahhaSettings(
                    env,
                    currentSettings.notificationSettings,
                    currentSettings.framework
                )
                setSahhaSettings(context, newSettings.toSahhaConfiguration())
                newSettings
            } ?: getSahhaSettings(context)

            if (!Sahha.diInitialized())
                Sahha.di = getDaggerAppComponent(context, settings)

            if (!Sahha.simInitialized())
                Sahha.sim = Sahha.di.sahhaInteractionManager

            val notificationConfig = Sahha.di.configurationDao.getNotificationConfig()
            Sahha.notificationManager.setNewPersistent(
                notificationConfig.icon,
                notificationConfig.title,
                notificationConfig.shortDescription,
            )
        }
    }

    private fun getDaggerAppComponent(context: Context, settings: SahhaSettings): AppComponent {
        return DaggerAppComponent.builder()
            .appModule(AppModule(settings.environment))
            .context(context)
            .build()
    }

    private suspend fun getSahhaSettings(context: Context): SahhaSettings {
        val db = SahhaDbUtility.getDb(context)
        val config = db.configurationDao().getConfig()
        return config.toSahhaSettings()
    }

    private suspend fun setSahhaSettings(
        context: Context,
        config: SahhaConfiguration
    ) {
        val db = SahhaDbUtility.getDb(context)
        db.configurationDao().saveConfig(config)
    }
}