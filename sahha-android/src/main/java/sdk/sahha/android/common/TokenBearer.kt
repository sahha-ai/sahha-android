package sdk.sahha.android.common

object TokenBearer {
    operator fun invoke(token: String): String {
        return "Bearer $token"
    }
}