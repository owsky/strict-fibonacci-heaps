package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.utils.fixListRemove
import heaps.strict_fibonacci_heap.utils.insertIntoCircularList
import heaps.strict_fibonacci_heap.utils.moveToActiveRoots

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

    // link z to y, y to x
    link(z, y)
    link(y, x)

    // make x the leftmost child of the root
    val root = heapRecord.root!!
    x.parent = root
    root.leftChild?.let { firstChild ->
        val lastChild = firstChild.left!!
        insertIntoCircularList(lastChild, x, firstChild)
    }
    root.leftChild = x

    // assign loss zero and rank one to x
    x.loss = 0u
    val zeroRank = heapRecord.rankList
    x.setRank(zeroRank)
    x.increaseRank()

    // assign loss zero and rank zero to y
    y.loss = 0u
    y.setRank(zeroRank)

    // adjust fix-list for x (changed rank)
    moveToActiveRoots(x, heapRecord)

    // adjust fix-list for y (remove from it if present)
    y.rankFixListRecord?.let { fixListNode ->
        fixListRemove(fixListNode, heapRecord)
        y.rankFixListRecord = null
    }
}

fun <T : Comparable<T>> canPerformRootDegreeReduction(heapRecord: HeapRecord<T>): Boolean {
    val firstChild = heapRecord.root?.leftChild ?: return false
    val fstLastChild = firstChild.left!!
    val sndLastChild = fstLastChild.left!!
    val trdLastChild = sndLastChild.left!!
    return fstLastChild !== sndLastChild &&
        fstLastChild !== trdLastChild &&
        sndLastChild !== trdLastChild &&
        !fstLastChild.isActive() &&
        !sndLastChild.isActive() &&
        !trdLastChild.isActive()
}

fun <T : Comparable<T>> performRootDegreeReduction(heapRecord: HeapRecord<T>) {
    val fstLastChild = heapRecord.root!!.leftChild!!
    val sndLastChild = fstLastChild.left!!
    val trdLastChild = sndLastChild.left!!
    rootDegreeReduction(fstLastChild, sndLastChild, trdLastChild, heapRecord)
}
