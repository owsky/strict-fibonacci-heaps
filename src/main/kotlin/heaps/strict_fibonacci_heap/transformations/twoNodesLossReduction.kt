package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.utils.sortPair
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Two Nodes Loss Reduction Transformation.
 *
 * Given two active nodes [a] and [b] with rank r and loss = 1, assume w.l.o.g. a.key < b.key. Link
 * [b] to [a] and set the loss of [a] and [b] to zero.
 *
 * T(n) = O(1)
 */
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

    logger.debug { "Performing a two nodes loss reduction with nodes ${a.item} and ${b.item}" }

    val (x, y) = sortPair(a, b)

    // link y to x
    link(y, x, heapRecord)

    // set loss of x and y to zero
    x.setLoss(0u, heapRecord)
    y.setLoss(0u, heapRecord)
}

/**
 * Returns whether a two nodes loss reduction is possible.
 *
 * T(n) = O(1)
 */
fun <T : Comparable<T>> canPerformTwoNodesLossReduction(heapRecord: HeapRecord<T>): Boolean {
    heapRecord.fixListPartFour?.let { n1 ->
        n1.right?.let { n2 ->
            return n1.rank === n2.rank && n1.node.loss == 1u && n2.node.loss == 1u
        }
    }
    return false
}

/**
 * Performs a two node loss reduction is possible.
 *
 * T(n) = O(1)
 */
fun <T : Comparable<T>> performTwoNodesLossReduction(heapRecord: HeapRecord<T>) {
    val n1 = heapRecord.fixListPartFour!!
    val n2 = n1.right!!
    twoNodesLossReduction(n1.node, n2.node, heapRecord)
}
