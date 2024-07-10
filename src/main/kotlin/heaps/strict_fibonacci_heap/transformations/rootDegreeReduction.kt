package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.utils.insertNodeBetween
import heaps.strict_fibonacci_heap.utils.removeFromSiblings
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Root Degree Reduction Transformation.
 *
 * Let [a], [b], [c] be the three rightmost passive linkable children of the root. Assume w.l.o.g.
 * a.key < b.key < c.key. Set [a] and [b] as active. Link [c] to [b] and [b] to [a]. Make [a] the
 * leftmost child of the root.
 *
 * T(n) = O(1)
 */
fun <T : Comparable<T>> rootDegreeReduction(
    a: NodeRecord<T>,
    b: NodeRecord<T>,
    c: NodeRecord<T>,
    heapRecord: HeapRecord<T>
) {
    if (!a.isPassiveLinkable() ||
        !b.isPassiveLinkable() ||
        !c.isPassiveLinkable() ||
        a.parent !== heapRecord.root ||
        b.parent !== heapRecord.root ||
        c.parent !== heapRecord.root)
        throw IllegalArgumentException(
            "Root degree reduction can only be performed on three passive and linkable children of the root")

    logger.debug {
        "Performing a root degree reduction with nodes ${a.item}, ${b.item} and ${c.item}"
    }

    // sort by key
    val (x, y, z) =
        listOf(a, b, c)
            .sortedBy { it.item }
            .let { (first, second, third) -> Triple(first, second, third) }

    // mark x and y as active
    x.setActiveFromPassive(heapRecord)
    y.setActiveFromPassive(heapRecord)

    // x is now an active root, so it becomes the leftmost child
    if (heapRecord.root!!.leftChild !== x) {
        removeFromSiblings(x)
        insertNodeBetween(heapRecord.root!!.leftChild!!.left!!, x, heapRecord.root!!.leftChild!!)
        heapRecord.root!!.leftChild = x
    }

    // link z to y, y to x
    link(z, y, heapRecord)
    link(y, x, heapRecord)

    // if heapRecord's non-linkable child is not set, set it to x
    if (heapRecord.nonLinkableChild == null) heapRecord.nonLinkableChild = x
}

/**
 * Returns whether a root degree reduction is possible.
 *
 * T(n) = O(1)
 */
fun <T : Comparable<T>> canPerformRootDegreeReduction(heapRecord: HeapRecord<T>): Boolean {
    // check if root has children
    val firstChild = heapRecord.root?.leftChild ?: return false

    val fstLastChild = firstChild.left!!
    val sndLastChild = fstLastChild.left!!
    val trdLastChild = sndLastChild.left!!

    // check whether children are all distinct
    if (fstLastChild === sndLastChild ||
        fstLastChild === trdLastChild ||
        sndLastChild === trdLastChild)
        return false

    // check whether all three children are passive and linkable
    return fstLastChild.isPassiveLinkable() &&
        sndLastChild.isPassiveLinkable() &&
        trdLastChild.isPassiveLinkable()
}

/**
 * Performs a root degree reduction is possible.
 *
 * T(n) = O(1)
 */
fun <T : Comparable<T>> performRootDegreeReduction(heapRecord: HeapRecord<T>) {
    val fstLastChild = heapRecord.root!!.leftChild!!.left!!
    val sndLastChild = fstLastChild.left!!
    val trdLastChild = sndLastChild.left!!
    rootDegreeReduction(fstLastChild, sndLastChild, trdLastChild, heapRecord)
}
