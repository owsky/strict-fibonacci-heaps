import benchmark.runBenchmark
import demo.demo

const val seed = 1234L
const val playDemo = false
const val benchmark = true

fun main() {
    if (benchmark) runBenchmark(seed)
    if (playDemo) demo(seed)
}
