package heaps.strict_fibonacci_heap.auxiliary_structures

class RankListRecord<T : Comparable<T>>(val rankNumber: Int) {
    var inc: RankListRecord<T>? = null
    var dec: RankListRecord<T>? = null
    var loss: FixListRecord<T>? = null
    var activeRoots: FixListRecord<T>? = null
    var refCount: Int = 0

    fun isLossTransformable(): Boolean {
        if (loss == null) return false

        val firstLoss = loss!!

        if (firstLoss.node.loss!! >= 2u) return true

        val secondLoss = loss!!.right!!
        return secondLoss.rank === loss!!.rank
    }
}
