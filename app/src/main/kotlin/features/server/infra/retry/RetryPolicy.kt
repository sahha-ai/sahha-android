package features.server.infra.retry

interface RetryPolicy {
    /**
     * @param attempt 0-based attempt count (0 == first retry attempt)
     * @param retryAfterMs when server provides a delay (e.g., Retry-After), prefer that
     * @return delay in ms, or null to stop retrying
     */
    fun nextDelayMs(attempt: Int, retryAfterMs: Long?): Long?
}