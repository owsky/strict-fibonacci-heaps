package heaps.strict_fibonacci_heap.auxiliary_structures

class FixListRecord<T : Comparable<T>>(
    val node: NodeRecord<T>,
    var left: FixListRecord<T>,
    var right: FixListRecord<T>,
    var rank: RankListRecord<T>
)
