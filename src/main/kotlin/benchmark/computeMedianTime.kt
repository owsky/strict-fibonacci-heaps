package benchmark

import kotlin.math.floor
import kotlin.time.Duration
import kotlin.time.measureTime

fun medianTime(n: Int, action: () -> Unit): Duration {
    require(n % 2 != 0) { "Use an odd n for perfect median" }
    val results = mutableListOf<Duration>()
    for (i in 1..n) results.add(measureTime(action))
    results.sort()
    return results[floor(n.toDouble() / 2).toInt()]
}
