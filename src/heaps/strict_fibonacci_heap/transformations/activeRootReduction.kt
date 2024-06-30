package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.utils.fixListMove
import heaps.strict_fibonacci_heap.utils.fixListRemove
import heaps.strict_fibonacci_heap.utils.moveToPositiveLoss

fun <T : Comparable<T>> activeRootReduction(
    n1: NodeRecord<T>,
    n2: NodeRecord<T>,
    heapRecord: HeapRecord<T>
) {
    if (!n1.isActiveRoot() || !n2.isActiveRoot())
        throw IllegalArgumentException("Nodes should be an active root")
    if (n1.getRank() !== n2.getRank())
        throw IllegalArgumentException("Nodes should have the same rank")

    val x = if (n1.item <= n2.item) n1 else n2
    val y = if (n1.item <= n2.item) n2 else n1

    link(y, x)
    x.increaseRank()

    x.leftChild?.let { leftChild ->
        val lastChild = leftChild.left
        if (!lastChild!!.isActive()) link(lastChild, heapRecord.root!!)
    }

    // adjust fix-list
    val firstFixList = heapRecord.fixList!!
    val secondFixList = firstFixList.right
    if (firstFixList.node.isActiveRoot() &&
        secondFixList.node.isActiveRoot() &&
        firstFixList.node.getRank() != secondFixList.node.getRank())
        fixListMove(heapRecord.singles!!.left, firstFixList, heapRecord.singles!!, heapRecord)

    // y is not an active root anymore
    if (y.loss!! == 0u) fixListRemove(y.rankFixListRecord!!, heapRecord)
    else moveToPositiveLoss(y, heapRecord)
}

fun <T : Comparable<T>> canPerformActiveRootReduction(heapRecord: HeapRecord<T>): Boolean {
    val lastInFix = heapRecord.fixList ?: return false
    val firstInFix = lastInFix.right
    val sndInFix = firstInFix.right

    return firstInFix !== sndInFix &&
        firstInFix.node.isActiveRoot() &&
        sndInFix.node.isActiveRoot() &&
        firstInFix.rank === sndInFix.rank
}

fun <T : Comparable<T>> performActiveRootReduction(heapRecord: HeapRecord<T>) {
    val firstInFix = heapRecord.fixList!!.right
    val sndInFix = firstInFix.right
    activeRootReduction(firstInFix.node, sndInFix.node, heapRecord)
}
