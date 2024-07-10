package heaps.strict_fibonacci_heap.auxiliary_structures

/**
 * Data class which defines the nodes of the fix-list, which holds active roots and nodes with
 * positive loss. The fix-list is defined in the original paper as a doubly-linked, circular linked
 * list. I have split it up into four separate (non-circular) doubly-linked lists for ease of use.
 * The part in which the node is stored is specified by [fixListPart].
 */
data class FixListRecord<T : Comparable<T>>(val node: NodeRecord<T>, var rank: RankListRecord<T>) {
    var left: FixListRecord<T>? = null
    var right: FixListRecord<T>? = null
    var fixListPart: FixListPart? = null
}
