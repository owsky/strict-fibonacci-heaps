@file:Suppress("UNCHECKED_CAST")

package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.FixListRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.utils.fixListRemove
import heaps.strict_fibonacci_heap.utils.moveToActiveRoots
import heaps.strict_fibonacci_heap.utils.moveToPositiveLoss

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
    if (a.loss!! != b.loss!! || a.loss!! == 1u)
        throw IllegalArgumentException(
            "Two nodes loss reduction can only be applied to nodes with loss equal to 1")

    val x: NodeRecord<T>
    val y: NodeRecord<T>
    if (a.item <= b.item) {
        x = a
        y = b
    } else {
        x = b
        y = a
    }

    val z = y.parent!!

    // link y to x
    link(y, x)

    // increase the rank of x
    x.increaseRank()

    // set loss of x and y to zero
    x.setLoss(0u)
    y.setLoss(0u)

    // decrease the rank of z
    z.decreaseRank()

    // if z is not an active root, the loss is increased by one
    if (!z.isActiveRoot()) z.setLoss(z.loss!! + 1u)

    // adjust fix-list for x and y (loss is now zero)
    fixListRemove(x.rank as FixListRecord<T>, heapRecord)
    fixListRemove(y.rank as FixListRecord<T>, heapRecord)

    // adjust fix-list for z (decreased rank, increased loss)
    if (z.isActiveRoot()) moveToActiveRoots(z, heapRecord) else moveToPositiveLoss(z, heapRecord)
}
