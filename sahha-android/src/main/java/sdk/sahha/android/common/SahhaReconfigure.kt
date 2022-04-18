package sdk.sahha.android.common

import androidx.activity.ComponentActivity
import sdk.sahha.android.Sahha
import sdk.sahha.android.di.AppModule

object SahhaReconfigure {
    suspend operator fun invoke(activity: ComponentActivity) {
        val configDao = AppModule.provideDatabase(activity).configurationDao()
        val storedSahhaSettings = configDao.getConfig().toSahhaSettings()
        Sahha.configure(activity, storedSahhaSettings)
    }
}