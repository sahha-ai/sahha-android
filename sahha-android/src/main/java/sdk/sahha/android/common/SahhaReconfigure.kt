package sdk.sahha.android.common

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import sdk.sahha.android.Sahha
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.di.ManualDependencies

object SahhaReconfigure {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend operator fun invoke(context: Context) {
        val settings =
            AppModule.provideDatabase(context).configurationDao().getConfig().toSahhaSettings()
        Sahha.di = ManualDependencies(context, settings.environment)
        Sahha.notifications.setNewPersistent(
            Sahha.di.configurationDao.getNotificationConfig().icon,
            Sahha.di.configurationDao.getNotificationConfig().title,
            Sahha.di.configurationDao.getNotificationConfig().shortDescription,
        )
    }
}