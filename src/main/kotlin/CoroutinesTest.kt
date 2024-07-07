import heaps.strict_fibonacci_heap.StrictFibonacciHeap
import kotlin.system.exitProcess
import kotlinx.coroutines.*
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle

fun main() = runBlocking {
    val nJobs = 100000
    val progressBar =
        ProgressBarBuilder()
            .setTaskName("Running tests")
            .setInitialMax(nJobs.toLong())
            .setStyle(ProgressBarStyle.ASCII)
            .build()

    val jobs =
        (1..nJobs).map {
            launch(Dispatchers.Default) {
                val nums = generateIntegers(100000, 0..10000000)
                try {
                    val h = StrictFibonacciHeap(nums)
                    val extracted = ArrayList<Int>()
                    for (i in nums.indices) extracted.add(h.extractMin())

                    val sortedNums = nums.sorted()
                    if (sortedNums != extracted)
                        throw IllegalStateException("The heap is returning the wrong items")
                    else progressBar.step()
                } catch (e: IllegalArgumentException) {
                    println(e.message)
                    println(nums)
                    exitProcess(1)
                }
            }
        }

    // Wait for all jobs to complete
    jobs.joinAll()
    progressBar.close()
}
