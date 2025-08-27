package core.config

enum class SahhaEnvironment(val baseUrl: String) {
    DEVELOPMENT("https://development-api.sahha.ai"),
    SANDBOX("https://sandbox-api.sahha.ai"),
    PRODUCTION("https://api.sahha.ai")
}