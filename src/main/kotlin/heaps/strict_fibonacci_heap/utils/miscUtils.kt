package heaps.strict_fibonacci_heap.utils

import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord

fun <T : Comparable<T>> sortPair(
    a: NodeRecord<T>,
    b: NodeRecord<T>
): Pair<NodeRecord<T>, NodeRecord<T>> {
    return if (a.item <= b.item) {
        a to b
    } else b to a
}
