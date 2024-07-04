import heaps.strict_fibonacci_heap.StrictFibonacciHeap
import visualization.visualizeTree
import visualization.visualizeTreeInteractive

fun main() {
    val randomGeneration = false
    val visualize = true
    val interactive = false
    val nums =
        if (randomGeneration) generateIntegers(1234L, 15, 0..100)
        else
            intArrayOf(17, 27, 28, 11, 10, 2, 30, 26, 1, 22, 23, 7, 13, 24, 3, 12, 25, 6, 29, 21)
                .toList()
    val h: StrictFibonacciHeap<Int>
    if (visualize) {
        if (interactive) {
            h = StrictFibonacciHeap()
            visualizeTreeInteractive(h, nums)
        } else {
            h = StrictFibonacciHeap(nums)
            visualizeTree(h)
        }
    } else {
        h = StrictFibonacciHeap(nums)
    }

    //    val extracted = ArrayList<Int>()
    //    for (i in nums.indices) extracted.add(h.extractMin())
    //    var sortedNums = nums.toMutableList()
    //    sortedNums.sort()
    //    sortedNums = sortedNums.subList(0, 5)
    //    if (sortedNums != extracted) throw IllegalStateException("Heap is returning the wrong
    // items")
    //    else {
    //        println(nums)
    //        println(extracted)
    //    }
}
