package heaps.strict_fibonacci_heap.auxiliary_structures

class NodeRecord<T : Comparable<T>>(var item: T) {
    var left: NodeRecord<T>? = null
    var right: NodeRecord<T>? = null
    var parent: NodeRecord<T>? = null
    var leftChild: NodeRecord<T>? = null
    private var active: ActiveRecord? = null
    var qPrev: NodeRecord<T>? = null
    var qNext: NodeRecord<T>? = null
    var loss: UInt? = null
    var rankRankListRecord: RankListRecord<T>? = null
    var rankFixListRecord: FixListRecord<T>? = null

    fun isActiveRoot(): Boolean {
        return isActive() && (parent != null && !parent!!.isActive())
    }

    fun isActive(): Boolean {
        return active?.flag ?: false
    }

    fun isPassive(): Boolean {
        return !isActive()
    }

    fun isPassiveLinkable(): Boolean {
        if (leftChild != null && leftChild!!.isActive()) return false
        return isPassive()
    }

    fun isInFixList(): Boolean {
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
        val currRank = getRank()
        val nextRank: RankListRecord<T>
        // if next rank doesn't exist, create it
        if (currRank.inc == null) {
            nextRank = RankListRecord(currRank.rankNumber + 1)
            currRank.inc = nextRank
            nextRank.dec = currRank
        } else {
            nextRank = currRank.inc!!
        }
        setRank(nextRank)
    }

    fun decreaseRank() {
        val prevRank: RankListRecord<T> =
            if (isInFixList()) rankFixListRecord!!.rank.dec!!
            else if (isActive()) rankRankListRecord!!
            else
                throw RuntimeException(
                    "Trying to decrease a node's rank whose rank is already zero")
        setRank(prevRank)
    }

    fun setRank(newRank: RankListRecord<T>) {
        val currRank = getRank()
        --currRank.refCount
        ++newRank.refCount
        if (isInFixList()) {
            val nextInFix = rankFixListRecord!!.right

            if (isActiveRoot()) {
                currRank.activeRoots?.let {
                    if (it === rankFixListRecord) {
                        currRank.activeRoots =
                            if (nextInFix.rank === currRank && nextInFix.node.isActiveRoot())
                                nextInFix
                            else null
                    }
                }
            } else {
                currRank.loss?.let {
                    if (it === rankFixListRecord) {
                        currRank.loss =
                            if (nextInFix.rank === currRank && nextInFix.node.isPassive()) nextInFix
                            else null
                    }
                }
            }
            rankFixListRecord!!.rank = newRank
            rankRankListRecord = null
        } else if (isActive()) {
            rankRankListRecord = newRank
            rankFixListRecord = null
        }
    }

    fun setActive(activeRecord: ActiveRecord, zeroRank: RankListRecord<T>) {
        active = activeRecord
        ++active!!.refCount
        rankFixListRecord = FixListRecord(this, zeroRank)
        rankRankListRecord = zeroRank
        ++rankRankListRecord!!.refCount
    }

    fun setPassive() {
        --active!!.refCount
        active = null
        val rank = getRank()
        --rank.refCount
    }
}
