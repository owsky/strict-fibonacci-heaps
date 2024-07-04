import heaps.strict_fibonacci_heap.StrictFibonacciHeap
import visualization.visualizeTree

fun main() {
    //    runDijkstra(HeapKind.BINARY_HEAP)
    //    runPrim(HeapKind.BINARY_HEAP)
    //    runDijkstra(HeapKind.FIBONACCI_HEAP)
    //    runPrim(HeapKind.FIBONACCI_HEAP)

    var nums = generateIntegers(1234L, 15, 0..100)
    nums =
        intArrayOf(17, 27, 28, 11, 10, 2, 30, 26, 1, 22, 23, 7, 13, 24, 3, 12, 25, 6, 29, 21)
            .toList()
    val h: StrictFibonacciHeap<Int> = StrictFibonacciHeap()
    val visualize = true
    nums.forEach {
        h.insert(it)
        if (visualize) visualizeTree(h)
    }

    //    visualizeTree(h)

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
