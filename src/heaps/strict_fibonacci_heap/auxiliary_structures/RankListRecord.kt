package heaps.strict_fibonacci_heap.auxiliary_structures

class RankListRecord<T : Comparable<T>>(val rankNumber: Int) {
    var inc: RankListRecord<T>? = null
    var dec: RankListRecord<T>? = null
    var loss: FixListRecord<T>? = null
    var activeRoots: FixListRecord<T>? = null
    var refCount: Int = 0
}
