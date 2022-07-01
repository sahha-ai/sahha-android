package sdk.sahha.android.common

import android.content.Context
import android.os.Build
import kotlinx.coroutines.delay
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.di.ManualDependencies
import sdk.sahha.android.domain.model.config.toSahhaSettings
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment

object SahhaReconfigure {
    suspend operator fun invoke(context: Context) {
        val settings =
            AppModule.provideDatabase(context).configurationDao().getConfig().toSahhaSettings()
        Sahha.di = ManualDependencies(settings.environment)
        Sahha.di.setDependencies(context)
        delay(250)

        if (Build.VERSION.SDK_INT < 26) return
        val notificationConfig = Sahha.di.configurationDao.getNotificationConfig()
        Sahha.notifications.setNewPersistent(
            notificationConfig.icon,
            notificationConfig.title,
            notificationConfig.shortDescription,
        )
    }
}