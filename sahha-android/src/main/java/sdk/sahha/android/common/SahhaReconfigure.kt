package sdk.sahha.android.common

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import sdk.sahha.android.data.local.SahhaDbUtility
import sdk.sahha.android.di.AppComponent
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.di.DaggerAppComponent
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.model.config.toSahhaSettings
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSettings

private const val TAG = "SahhaReconfigure"

internal object SahhaReconfigure {
    suspend operator fun invoke(
        context: Context,
        environment: SahhaEnvironment? = null
    ) {
        withContext(Main) {
            val env = environment ?: getEnvironmentSharedPrefs(context) ?: return@withContext
            saveEnv(context, env)

            if (!Sahha.diInitialized())
                Sahha.di = getDaggerAppComponent(context, env)

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

    private fun saveEnv(context: Context, env: Enum<SahhaEnvironment>) {
        val prefs =
            context.getSharedPreferences(Constants.CONFIGURATION_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(Constants.ENVIRONMENT_KEY, env.ordinal).apply()
    }

    private fun getEnvironmentSharedPrefs(context: Context): Enum<SahhaEnvironment>? {
        val prefs =
            context.getSharedPreferences(Constants.CONFIGURATION_PREFS, Context.MODE_PRIVATE)
        val envInt = prefs.getInt(Constants.ENVIRONMENT_KEY, -1)

        return when {
            envInt == -1 -> null
            else -> SahhaEnvironment.values()[envInt]
        }
    }

    private fun getDaggerAppComponent(
        context: Context,
        environment: Enum<SahhaEnvironment>
    ): AppComponent {
        return DaggerAppComponent.builder()
            .appModule(AppModule(environment))
            .context(context)
            .build()
    }
}