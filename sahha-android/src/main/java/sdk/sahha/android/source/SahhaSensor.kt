package sdk.sahha.android.source

import androidx.annotation.Keep
import com.google.android.gms.common.annotation.KeepName

@Keep
enum class SahhaSensor {
    device,
    sleep,
    pedometer,
    heart,
    blood
}