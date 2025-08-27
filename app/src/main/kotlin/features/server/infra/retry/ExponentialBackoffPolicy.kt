package features.server.infra.retry

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class ExponentialBackoffPolicy(
    private val baseMs: Long = 500,
    private val maxMs: Long = 30_000,
    private val maxAttempts: Int = 6,
    private val jitterPct: Double = 0.2
) : RetryPolicy {
    override fun nextDelayMs(attempt: Int, retryAfterMs: Long?): Long? {
        if (attempt >= maxAttempts) return null
        retryAfterMs?.let { return it }
        val expo = min(baseMs shl attempt, maxMs)
        val jitter = (expo * jitterPct * (Random.nextDouble(-1.0, 1.0))).toLong()
        return max(0, expo + jitter)
    }
}