package heaps.strict_fibonacci_heap.auxiliary_structures

class FixListRecord<T : Comparable<T>>(val node: NodeRecord<T>, var rank: RankListRecord<T>) {
    var left: FixListRecord<T> = this
    var right: FixListRecord<T> = this
}
