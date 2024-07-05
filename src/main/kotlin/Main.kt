import heaps.strict_fibonacci_heap.StrictFibonacciHeap
import visualization.visualizeTree
import visualization.visualizeTreeInteractive

val randomGeneration = false
var visualize = true
val interactive = true
val shortcut = true
var nums =
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

fun main() {
    //    if (isDebuggerAttached()) visualize = false

    val h: StrictFibonacciHeap<Int>
    if (visualize) {
        if (shortcut) {
            //            nums = nums.subList(0, 12)
            h = StrictFibonacciHeap(nums)
            visualizeTreeInteractive(h, nums)
        } else if (interactive) {
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

    val sortedNums = nums.toMutableList()
    sortedNums.sort()
    if (sortedNums != extracted)
        throw IllegalStateException("The heap is returning the wrong items")
    else {
        println(nums)
        println(extracted)
    }
    visualizeTree(h)
}
