package heaps.strict_fibonacci_heap.utils

import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord

fun <T : Comparable<T>> insertIntoCircularList(
    a: NodeRecord<T>,
    x: NodeRecord<T>,
    b: NodeRecord<T>
) {
    a.right = x
    b.left = x
    x.left = a
    x.right = b
}

fun <T : Comparable<T>> removeFromCircularList(x: NodeRecord<T>) {
    val a = x.left
    val b = x.right
    if (a != x) {
        a?.right = b
        b?.right = a
    }
    x.left = null
    x.right = null
}
