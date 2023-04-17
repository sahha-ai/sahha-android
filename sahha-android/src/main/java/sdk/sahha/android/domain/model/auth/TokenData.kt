package sdk.sahha.android.domain.model.auth

import androidx.annotation.Keep

@Keep
data class TokenData(
    val profileToken: String,
    val refreshToken: String
)
