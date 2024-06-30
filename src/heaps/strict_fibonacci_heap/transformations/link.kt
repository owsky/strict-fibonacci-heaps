package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.utils.insertIntoCircularList
import heaps.strict_fibonacci_heap.utils.removeFromCircularList

fun <T : Comparable<T>> link(x: NodeRecord<T>, y: NodeRecord<T>) {
    x.parent = y
    removeFromCircularList(x)

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
