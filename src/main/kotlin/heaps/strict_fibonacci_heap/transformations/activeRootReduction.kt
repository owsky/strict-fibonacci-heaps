package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.utils.sortPair
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Active Root Reduction transformation.
 *
 * Let x and y be active roots of equal rank r. Assume w.l.o.g. x.key < y.key. Link y to x. If the
 * rightmost child z of x is passive, make z a child of the root.
 *
 * T(n) = O(1)
 */
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
    link(y, x, heapRecord)

    x.leftChild?.let { firstChild ->
        val lastChild = firstChild.left!!
        if (lastChild.isPassive()) link(lastChild, heapRecord.root!!, heapRecord)
    }
}

/**
 * Returns whether an active root reduction is possible.
 *
 * T(n) = O(1)
 */
fun <T : Comparable<T>> canPerformActiveRootReduction(heapRecord: HeapRecord<T>): Boolean {
    return heapRecord.fixListPartOne != null
}

/**
 * Performs an active root reduction.
 *
 * T(n) = O(1)
 */
fun <T : Comparable<T>> performActiveRootReduction(heapRecord: HeapRecord<T>) {
    val n1 = heapRecord.fixListPartOne!!
    val n2 = n1.right!!
    activeRootReduction(n1.node, n2.node, heapRecord)
}
