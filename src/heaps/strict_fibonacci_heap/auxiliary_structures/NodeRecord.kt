package heaps.strict_fibonacci_heap.auxiliary_structures

class NodeRecord<T : Comparable<T>>(val item: T) {
    var left: NodeRecord<T>? = null
    var right: NodeRecord<T>? = null
    var parent: NodeRecord<T>? = null
    var leftChild: NodeRecord<T>? = null
    var active: ActiveRecord? = null
    var qPrev: NodeRecord<T>? = null
    var qNext: NodeRecord<T>? = null
    var loss: UInt? = null
    var rankRankListRecord: RankListRecord<T>? = null
    var rankFixListRecord: FixListRecord<T>? = null

    fun isActiveRoot(): Boolean {
        return isActive() && (parent?.isActive() ?: false)
    }

    fun isActive(): Boolean {
        return active?.flag ?: false
    }

    private fun isInFixList(): Boolean {
        return isActiveRoot() || (isActive() && loss != null && loss!! > 0u)
    }

    fun getRank(): RankListRecord<T> {
        return if (isInFixList()) rankFixListRecord!!.rank
        else if (isActive()) rankRankListRecord!!
        else
            throw IllegalAccessException(
                "Trying to access the rank pointer for a node which isn't an active root nor an active node with positive loss")
    }

    fun increaseRank() {
        var nextRank: RankListRecord<T>? = null
        if (isInFixList()) nextRank = rankFixListRecord!!.rank.inc ?: RankListRecord()
        else if (isActive()) nextRank = rankRankListRecord!!.inc ?: RankListRecord()
        setRank(nextRank)
    }

    fun decreaseRank() {
        var prevRank: RankListRecord<T>? = null
        if (isInFixList()) prevRank = rankFixListRecord!!.rank.dec!!
        else if (isActive()) prevRank = rankRankListRecord!!
        setRank(prevRank)
    }

    fun setRank(newRank: RankListRecord<T>?) {
        if (newRank == null)
            throw IllegalAccessException(
                "Trying to access the rank pointer for a node which isn't an active root nor an active node with positive loss")
        if (isInFixList()) {
            val fix = rankFixListRecord!!
            --fix.rank.refCount
            ++newRank.refCount
            rankFixListRecord!!.rank = newRank
        } else if (isActive()) {
            val prevRank = rankRankListRecord!!
            --prevRank.refCount
            ++newRank.refCount
            rankRankListRecord = newRank
        }
    }
}
