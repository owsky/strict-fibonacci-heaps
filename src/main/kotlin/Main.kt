import benchmark.runBenchmark
import demo.demo

const val seed = 1234L
const val playDemo = true
const val benchmark = false

fun main() {
    if (benchmark) runBenchmark(seed)
    if (playDemo) demo(seed)
}
