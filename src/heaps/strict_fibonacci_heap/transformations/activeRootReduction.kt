package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.utils.fixListRemove
import heaps.strict_fibonacci_heap.utils.moveToActiveRoots

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

    val yPrevParent = y.parent

    // y is not going to be an active root anymore, and it had loss zero because active roots have
    // no loss, so remove from the fix-list
    fixListRemove(y.rankFixListRecord!!, heapRecord)

    // link y to x, x.item < y.item
    link(y, x)

    yPrevParent?.let {
        if (it.isPassiveLinkable() && heapRecord.nonLinkableChild == null)
            heapRecord.nonLinkableChild = it
    }

    x.leftChild?.let { firstChild ->
        val lastChild = firstChild.left
        if (!lastChild!!.isActive()) {
            link(lastChild, heapRecord.root!!)
            if (heapRecord.nonLinkableChild == null) heapRecord.nonLinkableChild = lastChild
        }
    }

    // if x is the last active root of its rank in part 1, move it to part 2
    val xFix = x.rankFixListRecord!!
    val nextInFix = xFix.right
    if (!nextInFix.node.isActiveRoot() || xFix.rank !== nextInFix.rank)
        moveToActiveRoots(x, heapRecord)
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
