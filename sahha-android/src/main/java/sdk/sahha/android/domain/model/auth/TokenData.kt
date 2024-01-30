package sdk.sahha.android.domain.model.auth

import androidx.annotation.Keep

@Keep
internal data class TokenData(
    val profileToken: String,
    val refreshToken: String
)
