package sdk.sahha.android.common

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import sdk.sahha.android.data.local.SahhaDbUtility
import sdk.sahha.android.di.AppComponent
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.di.DaggerAppComponent
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment

private const val TAG = "SahhaReconfigure"

internal object SahhaReconfigure {
    suspend operator fun invoke(
        context: Context,
        environment: SahhaEnvironment? = null
    ) {
        withContext(Main) {
            val env = environment ?: getEnvironmentSharedPrefs(context)
            ?: getEnvironmentDb(context)
            ?: SahhaEnvironment.sandbox

            saveEnv(context, env)

            if (!Sahha.diInitialized())
                Sahha.di = getDaggerAppComponent(context, env)

            if (!Sahha.simInitialized())
                Sahha.sim = Sahha.di.sahhaInteractionManager

            try {
                val notificationConfig = Sahha.di.configurationDao.getNotificationConfig()
                notificationConfig?.also {
                    Sahha.notificationManager.setNewPersistent(
                        it.icon,
                        it.title,
                        it.shortDescription,
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, e.message ?: "Could not set notification configuration")
            }
        }
    }

    private suspend fun getEnvironmentDb(context: Context): Enum<SahhaEnvironment>? {
        val dao = SahhaDbUtility.getDb(context).configurationDao()
        val config = dao.getConfig() ?: return null

        val envInt = config.environment
        return SahhaEnvironment.values()[envInt]
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