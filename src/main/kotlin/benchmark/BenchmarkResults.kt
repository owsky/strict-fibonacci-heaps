package benchmark

import kotlin.time.Duration

data class BenchmarkResults(
    val algorithm: String,
    val n: Int,
    val p: Double,
    val heapKind: String,
    val runtime: Duration
) {
    fun toList(): List<Any> {
        val adjustedRuntime = runtime.inWholeMicroseconds.toDouble() / 1000
        val formattedRuntime = String.format("%.2f", adjustedRuntime)
        return listOf(n, p, heapKind, formattedRuntime)
    }
}
