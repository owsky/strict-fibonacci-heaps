package heaps.strict_fibonacci_heap.auxiliary_structures

class FixListRecord<T : Comparable<T>>(
    node: NodeRecord<T>,
    left: FixListRecord<T>,
    right: FixListRecord<T>,
    rank: RankListRecord<T>
)
