package benchmark

import benchmark.excel_output.outputToExcel

fun runBenchmark(seed: Long) {
    val results = measureRuntimes(seed)

    val sortedResults = results.sortedWith(compareBy({ it.n }, { it.p }, { it.heapKind }))

    outputToExcel(sortedResults)
}
