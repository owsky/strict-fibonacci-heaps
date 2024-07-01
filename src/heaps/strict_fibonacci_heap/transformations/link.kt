package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.utils.insertIntoCircularList
import heaps.strict_fibonacci_heap.utils.removeFromCircularList

fun <T : Comparable<T>> link(x: NodeRecord<T>, y: NodeRecord<T>) {
    val previousParent = x.parent
    x.parent = y
    removeFromCircularList(x)

    // if both x and its previous parent are active, decrease the parent rank
    previousParent?.let { if (x.isActive() && it.isActive()) it.decreaseRank() }

    // if the previous parent is active, increase its loss
    previousParent?.let { if (it.isActive()) it.loss = it.loss!! + 1u }

    // if both x and y are active, increase the rank of y
    if (x.isActive() && y.isActive()) y.increaseRank()

    if (y.leftChild == null) {
        // if y has no children
        y.leftChild = x
        x.left = x
        x.right = x
    } else {
        // if y has at least one child
        val firstChild = y.leftChild!!
        val lastChild = firstChild.left!!
        insertIntoCircularList(lastChild, x, firstChild)
        if (x.isActive()) y.leftChild = x
    }
}
