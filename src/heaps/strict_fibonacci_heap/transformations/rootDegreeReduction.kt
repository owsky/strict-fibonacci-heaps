package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.utils.insertIntoCircularList
import heaps.strict_fibonacci_heap.utils.moveToActiveRoots
import heaps.strict_fibonacci_heap.utils.removeFromCircularList

fun <T : Comparable<T>> rootDegreeReduction(
    a: NodeRecord<T>,
    b: NodeRecord<T>,
    c: NodeRecord<T>,
    heapRecord: HeapRecord<T>
) {
    // sort by key
    val (x, y, z) =
        listOf(a, b, c)
            .sortedBy { it.item }
            .let { (first, second, third) -> Triple(first, second, third) }

    // mark x and y as active
    x.setActive(heapRecord.activeRecord, heapRecord.rankList)
    y.setActive(heapRecord.activeRecord, heapRecord.rankList)

    // check if non-linkable child needs update
    heapRecord.nonLinkableChild?.let { if (it.isActive()) heapRecord.nonLinkableChild = null }

    // x is now an active root, so it becomes the leftmost child
    if (heapRecord.root!!.leftChild !== x) {
        removeFromCircularList(x)
        insertIntoCircularList(
            heapRecord.root!!.leftChild!!.left!!, x, heapRecord.root!!.leftChild!!)
        heapRecord.root!!.leftChild = x
    }

    // link z to y, y to x
    link(z, y)
    link(y, x)

    // assign loss zero and rank one to x
    x.setLoss(0u)
    val zeroRank = heapRecord.rankList
    x.setRank(zeroRank)
    x.increaseRank()

    // assign loss zero and rank zero to y
    y.setLoss(0u)
    y.setRank(zeroRank)

    // adjust fix-list for x (changed rank)
    x.setActiveRoot()
    moveToActiveRoots(x, heapRecord)
}

fun <T : Comparable<T>> canPerformRootDegreeReduction(heapRecord: HeapRecord<T>): Boolean {
    // check if root has children
    val firstChild = heapRecord.root?.leftChild ?: return false

    // check if non-linkable child exists
    val nonLinkableChild = heapRecord.nonLinkableChild ?: return false

    if (nonLinkableChild.parent !== heapRecord.root!!)
        throw RuntimeException("Non-linkable child points to a node whose parent is not the root")

    val fstLastChild = firstChild.left!!
    val sndLastChild = fstLastChild.left!!
    val trdLastChild = sndLastChild.left!!

    // check whether children are all distinct
    if (fstLastChild === sndLastChild ||
        fstLastChild === trdLastChild ||
        sndLastChild === trdLastChild)
        return false

    // check whether last and second last children are the first non-linkable child
    if (sndLastChild === nonLinkableChild || fstLastChild === nonLinkableChild) return false

    // check whether all three children are passive and linkable
    return fstLastChild.isPassiveLinkable() &&
        sndLastChild.isPassiveLinkable() &&
        trdLastChild.isPassiveLinkable()
}

fun <T : Comparable<T>> performRootDegreeReduction(heapRecord: HeapRecord<T>) {
    val fstLastChild = heapRecord.root!!.leftChild!!.left!!
    val sndLastChild = fstLastChild.left!!
    val trdLastChild = sndLastChild.left!!
    rootDegreeReduction(fstLastChild, sndLastChild, trdLastChild, heapRecord)
}
