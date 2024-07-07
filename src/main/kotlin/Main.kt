import heaps.strict_fibonacci_heap.StrictFibonacciHeap
import visualization.visualizeTree
import visualization.visualizeTreeInteractive

const val visualize = false
const val interactive = true
const val shortcut = true

fun main() {

    val nums = generateIntegers(100000, 0..10000000)

    val h: StrictFibonacciHeap<Int>
    if (visualize) {
        if (shortcut) {
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

    val sortedNums = nums.sorted()
    if (sortedNums != extracted)
        throw IllegalStateException("The heap is returning the wrong items")
    else println("Success")
}
