package sdk.sahha.android.domain.model.dto.send

import androidx.annotation.Keep

@Keep
internal data class RefreshTokenSendDto(
    val refreshToken: String
)