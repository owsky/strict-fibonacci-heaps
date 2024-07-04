@file:Suppress("UNCHECKED_CAST")

package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.utils.sortPair

fun <T : Comparable<T>> twoNodesLossReduction(
    a: NodeRecord<T>,
    b: NodeRecord<T>,
    heapRecord: HeapRecord<T>
) {
    if (!a.isActive() || !b.isActive())
        throw IllegalArgumentException(
            "Two nodes loss reduction can only be applied to active nodes")
    if (a.getRank() !== b.getRank())
        throw IllegalArgumentException(
            "Two nodes loss reduction can only be applied to active nodes of the same rank")
    if (a.loss!! != b.loss!! || a.loss!! != 1u)
        throw IllegalArgumentException(
            "Two nodes loss reduction can only be applied to nodes with loss equal to 1")

    println("Performing a two nodes loss reduction with nodes ${a.item} and ${b.item}")

    val (x, y) = sortPair(a, b)

    val z = y.parent!!

    // link y to x
    link(y, x, heapRecord)

    //    // set loss of x and y to zero
    //    x.setLoss(0u, heapRecord)
    //    y.setLoss(0u, heapRecord)
    //
    //    // adjust fix-list for x and y (loss is now zero)
    //    fixListRemove(x.rank as FixListRecord<T>, heapRecord)
    //    fixListRemove(y.rank as FixListRecord<T>, heapRecord)
    //
    //    // adjust fix-list for z (decreased rank, increased loss)
    //    if (z.isActiveRoot()) moveToActiveRoots(z, heapRecord) else moveToPositiveLoss(z,
    // heapRecord)
}

fun <T : Comparable<T>> canPerformTwoNodesLossReduction(heapRecord: HeapRecord<T>): Boolean {
    heapRecord.fixList?.let { fstLastInFix ->
        val sndLastInFix = fstLastInFix.left!!
        return fstLastInFix !== sndLastInFix &&
            fstLastInFix.rank === sndLastInFix.rank &&
            fstLastInFix.node.loss == 1u &&
            sndLastInFix.node.loss == 1u
    }
    return false
}

fun <T : Comparable<T>> performTwoNodesLossReduction(heapRecord: HeapRecord<T>) {
    val fstLastInFix = heapRecord.fixList!!
    val sndLastInFix = fstLastInFix.left!!
    twoNodesLossReduction(fstLastInFix.node, sndLastInFix.node, heapRecord)
}
