package sdk.sahha.android.common

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import sdk.sahha.android.BuildConfig
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.di.ManualDependencies
import sdk.sahha.android.domain.model.config.toSahhaSettings

object SahhaReconfigure {
    suspend operator fun invoke(context: Context) {
        val settings =
            AppModule.provideDatabase(context).configurationDao().getConfig().toSahhaSettings()
        Sahha.di = ManualDependencies(settings.environment)
        Sahha.di.setDependencies(context)

        if(Build.VERSION.SDK_INT < 26) return
        Sahha.notifications.setNewPersistent(
            Sahha.di.configurationDao.getNotificationConfig().icon,
            Sahha.di.configurationDao.getNotificationConfig().title,
            Sahha.di.configurationDao.getNotificationConfig().shortDescription,
        )
    }
}