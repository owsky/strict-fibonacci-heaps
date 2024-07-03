package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.utils.moveToActiveRoots
import heaps.strict_fibonacci_heap.utils.sortPair

fun <T : Comparable<T>> activeRootReduction(
    n1: NodeRecord<T>,
    n2: NodeRecord<T>,
    heapRecord: HeapRecord<T>
) {
    if (!n1.isActiveRoot() || !n2.isActiveRoot())
        throw IllegalArgumentException("Nodes should be an active root")
    if (n1.getRank() !== n2.getRank())
        throw IllegalArgumentException("Nodes should have the same rank")

    println("Performing an active root reduction with nodes ${n1.item} and ${n2.item}")

    val (x, y) = sortPair(n1, n2)

    // link y to x, x.item < y.item
    link(y, x, heapRecord)

    // possibly adjust the fix-list
    moveToActiveRoots(x, heapRecord)
}

fun <T : Comparable<T>> canPerformActiveRootReduction(heapRecord: HeapRecord<T>): Boolean {
    val lastInFix = heapRecord.fixList ?: return false
    val firstInFix = lastInFix.right!!
    val sndInFix = firstInFix.right!!

    return firstInFix !== sndInFix &&
        firstInFix.node.isActiveRoot() &&
        sndInFix.node.isActiveRoot() &&
        firstInFix.rank === sndInFix.rank
}

fun <T : Comparable<T>> performActiveRootReduction(heapRecord: HeapRecord<T>) {
    val firstInFix = heapRecord.fixList!!.right!!
    val sndInFix = firstInFix.right!!
    activeRootReduction(firstInFix.node, sndInFix.node, heapRecord)
}
