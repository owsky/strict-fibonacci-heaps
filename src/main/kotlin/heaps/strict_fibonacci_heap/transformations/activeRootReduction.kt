package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.utils.sortPair
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun <T : Comparable<T>> activeRootReduction(
    n1: NodeRecord<T>,
    n2: NodeRecord<T>,
    heapRecord: HeapRecord<T>
) {
    if (!n1.isActiveRoot())
        throw IllegalArgumentException(
            "Trying to perform an active root reduction between nodes ${n1.item} and ${n2.item} but ${n1.item} is not an active root")
    if (!n2.isActiveRoot())
        throw IllegalArgumentException(
            "Trying to perform an active root reduction between nodes ${n1.item} and ${n2.item} but ${n2.item} is not an active root")
    if (n1.getRank() !== n2.getRank())
        throw IllegalArgumentException("Nodes should have the same rank")

    logger.debug { "Performing an active root reduction with nodes ${n1.item} and ${n2.item}" }

    val (x, y) = sortPair(n1, n2)

    // link y to x, x.item < y.item
    link(y, x, heapRecord)

    x.leftChild?.let { firstChild ->
        val lastChild = firstChild.left!!
        if (lastChild.isPassive()) link(lastChild, heapRecord.root!!, heapRecord)
    }
}

fun <T : Comparable<T>> canPerformActiveRootReduction(heapRecord: HeapRecord<T>): Boolean {
    return heapRecord.fixListPartOne != null
}

fun <T : Comparable<T>> performActiveRootReduction(heapRecord: HeapRecord<T>) {
    val n1 = heapRecord.fixListPartOne!!
    val n2 = n1.right!!
    activeRootReduction(n1.node, n2.node, heapRecord)
}
