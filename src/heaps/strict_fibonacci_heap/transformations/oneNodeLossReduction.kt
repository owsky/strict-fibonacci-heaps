package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.utils.moveToActiveRoots
import heaps.strict_fibonacci_heap.utils.moveToPositiveLoss

fun <T : Comparable<T>> oneNodeLossReduction(x: NodeRecord<T>, heapRecord: HeapRecord<T>) {
    if (x.loss == null || x.loss!! < 2u)
        throw IllegalArgumentException(
            "Trying to perform one node loss reduction on a node which doesn't have >= 2 loss")

    val y = x.parent!!
    val root = heapRecord.root!!

    // link x to the root
    link(x, root)

    // set x's loss to zero
    x.loss = 0u

    // decrease y's rank by one
    y.decreaseRank()

    // if y is not an active root, increase its loss by one
    if (!y.isActiveRoot()) y.loss = y.loss!! + 1u

    // adjust fix-list for x (it was an active node, now it's an active root)
    moveToActiveRoots(x, heapRecord)

    // adjust fix-list for y
    moveToPositiveLoss(y, heapRecord)
}
