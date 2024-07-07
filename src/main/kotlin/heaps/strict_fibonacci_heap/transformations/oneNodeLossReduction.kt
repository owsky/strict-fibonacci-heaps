package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun <T : Comparable<T>> oneNodeLossReduction(x: NodeRecord<T>, heapRecord: HeapRecord<T>) {
    if (x.loss == null || x.loss!! < 2u)
        throw IllegalArgumentException(
            "Trying to perform one node loss reduction on a node which doesn't have >= 2 loss")

    logger.debug { "Performing a one node loss reduction with node ${x.item}" }

    val root = heapRecord.root!!

    // link x to the root
    link(x, root, heapRecord)
}

fun <T : Comparable<T>> canPerformOneNodeLossReduction(heapRecord: HeapRecord<T>): Boolean {
    return heapRecord.fixListPartFour != null && heapRecord.fixListPartFour!!.node.loss!! >= 2u
}

fun <T : Comparable<T>> performOneNodeLossReduction(heapRecord: HeapRecord<T>) {
    oneNodeLossReduction(heapRecord.fixListPartFour!!.node, heapRecord)
}
