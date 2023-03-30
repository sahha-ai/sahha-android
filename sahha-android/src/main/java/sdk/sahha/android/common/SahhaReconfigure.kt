package sdk.sahha.android.common

import android.content.Context
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.di.ManualDependencies
import sdk.sahha.android.domain.model.config.toSahhaSettings
import sdk.sahha.android.source.Sahha

object SahhaReconfigure {
    suspend operator fun invoke(context: Context) {
        withContext(Main) {
            val settings =
                AppModule.provideDatabase(context).configurationDao().getConfig().toSahhaSettings()
            if (!Sahha.diInitialized())
                Sahha.di = ManualDependencies(settings.environment)
            Sahha.di.setDependencies(context)

            val notificationConfig = Sahha.di.configurationDao.getNotificationConfig()
            Sahha.notificationManager.setNewPersistent(
                notificationConfig.icon,
                notificationConfig.title,
                notificationConfig.shortDescription,
            )
        }
    }
}