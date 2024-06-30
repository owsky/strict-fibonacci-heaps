package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.utils.fixListMove

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
        fixListMove(heapRecord.singles!!.left, firstFixList, heapRecord.singles!!)
}
