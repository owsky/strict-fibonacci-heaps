package heaps.strict_fibonacci_heap.auxiliary_structures

class FixListRecord<T : Comparable<T>>(val node: NodeRecord<T>, var rank: RankListRecord<T>) {
    var left: FixListRecord<T>? = null
    var right: FixListRecord<T>? = null
}
