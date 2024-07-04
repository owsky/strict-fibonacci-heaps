import heaps.strict_fibonacci_heap.StrictFibonacciHeap
import visualization.visualizeTree
import visualization.visualizeTreeInteractive

fun main() {
    val randomGeneration = false
    val visualize = false
    val interactive = false
    val nums =
        if (randomGeneration) generateIntegers(1234L, 30, 0..100)
        else
            intArrayOf(
                    82,
                    23,
                    85,
                    31,
                    56,
                    95,
                    94,
                    47,
                    8,
                    50,
                    30,
                    40,
                    81,
                    59,
                    1,
                    98,
                    54,
                    27,
                    17,
                    21,
                    99,
                    75,
                    35,
                    4,
                    38,
                    22,
                    71,
                    12,
                    61,
                    14)
                .toList()
    println(nums)
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

    val extracted = ArrayList<Int>()
    for (i in nums.indices) extracted.add(h.extractMin())
    var sortedNums = nums.toMutableList()
    sortedNums.sort()
    sortedNums = sortedNums.subList(0, 5)
    if (sortedNums != extracted)
        throw IllegalStateException("The heap is returning the wrong items")
    else {
        println(nums)
        println(extracted)
    }
}
