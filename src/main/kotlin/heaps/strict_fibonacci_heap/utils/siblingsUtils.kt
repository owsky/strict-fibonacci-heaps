package heaps.strict_fibonacci_heap.utils

import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord

/**
 * Inserts the given node [x] into a circular list, in-between [a] and [b].
 *
 * T(n) = O(1)
 */
fun <T : Comparable<T>> insertNodeBetween(a: NodeRecord<T>, x: NodeRecord<T>, b: NodeRecord<T>) {
    a.right = x
    b.left = x
    x.left = a
    x.right = b
}

/**
 * Removes the given node [x] from its circular list.
 *
 * T(n) = O(1)
 */
fun <T : Comparable<T>> removeFromSiblings(x: NodeRecord<T>) {
    val a = x.left
    val b = x.right
    if (a != null && b != null && a != x) {
        if (a.right !== x || b.left !== x)
            throw RuntimeException("The left and right pointers are mismatched")
        a.right = b
        b.left = a
    }
    x.left = null
    x.right = null
}

/**
 * Inserts [x] as the new first child of [newParent].
 *
 * T(n) = O(1)
 */
fun <T : Comparable<T>> insertAsFirstSibling(x: NodeRecord<T>, newParent: NodeRecord<T>) {
    insertAsLastSibling(x, newParent)
    newParent.leftChild = x
}

/**
 * Inserts [x] as the new last child of [newParent].
 *
 * T(n) = O(1)
 */
fun <T : Comparable<T>> insertAsLastSibling(x: NodeRecord<T>, newParent: NodeRecord<T>) {
    val firstChild = newParent.leftChild!!
    val lastChild = firstChild.left!!
    insertNodeBetween(lastChild, x, firstChild)
}
