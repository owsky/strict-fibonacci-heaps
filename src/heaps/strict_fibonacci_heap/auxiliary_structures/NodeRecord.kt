@file:Suppress("UNCHECKED_CAST")

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
        private set

    // rank pointer needs to be either a pointer to RankListRecord or FixListRecord, unfortunately
    // Kotlin does not support union types
    var rank: Any? = null
        private set(value) {
            when (value) {
                is RankListRecord<*>,
                is FixListRecord<*>,
                null -> field = value
                else -> throw IllegalArgumentException("Unsupported rank type")
            }
        }

    fun processRank(fn: () -> Any, kind: RankEnum): Any {
        if ((kind == RankEnum.RANK_LIST_RECORD && rank is RankListRecord<*>) ||
            (kind == RankEnum.FIX_LIST_RECORD && rank is FixListRecord<*>)) {
            return fn()
        } else {
            throw IllegalAccessException("Trying to access the wrong rank pointer")
        }
    }

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

    fun increaseRank() {
        val nextRank: RankListRecord<T>
        when (rank) {
            is RankListRecord<*> -> {
                val rankRecord = rank as RankListRecord<T>
                if (rankRecord.inc == null) {
                    // if next rank doesn't exist, create it
                    nextRank = RankListRecord(rankRecord.rankNumber + 1)
                    rankRecord.inc = nextRank
                    nextRank.dec = rankRecord
                } else {
                    nextRank = rankRecord.inc!!
                }
            }
            is FixListRecord<*> -> {
                val fixRecord = rank as FixListRecord<T>
                val rankRecord = fixRecord.rank
                if (rankRecord.inc == null) {
                    // if next rank doesn't exist, create it
                    nextRank = RankListRecord(rankRecord.rankNumber + 1)
                    rankRecord.inc = nextRank
                    nextRank.dec = rankRecord
                } else {
                    nextRank = rankRecord.inc!!
                }
            }
            else -> {
                throw IllegalArgumentException("Unknown record type")
            }
        }
        setRank(nextRank)
    }

    fun decreaseRank() {
        val prevRank: RankListRecord<T>
        when (rank) {
            is RankListRecord<*> -> {
                val rankRecord = rank as RankListRecord<T>
                prevRank = rankRecord.dec!!
            }
            is FixListRecord<*> -> {
                val fixRecord = rank as FixListRecord<T>
                val rankRecord = fixRecord.rank
                prevRank = rankRecord.dec!!
            }
            else -> {
                throw IllegalArgumentException("Unknown record type")
            }
        }
        setRank(prevRank)
    }

    fun getRank(): RankListRecord<T> {
        return when (rank) {
            is RankListRecord<*> -> {
                rank as RankListRecord<T>
            }
            is FixListRecord<*> -> {
                (rank as FixListRecord<T>).rank
            }
            else -> {
                throw IllegalArgumentException("Unknown record type")
            }
        }
    }

    fun setRank(newRank: RankListRecord<T>?) {
        val currRank = getRank()
        --currRank.refCount

        if (newRank == null) {
            rank = null
            return
        }

        ++newRank.refCount
        when (rank) {
            is RankListRecord<*>,
            null -> {
                rank = newRank
            }
            is FixListRecord<*> -> {
                val rankFix = rank as FixListRecord<T>
                val nextInFix = rankFix.right

                if (isActiveRoot()) {
                    currRank.activeRoots?.let {
                        if (it === rankFix) {
                            currRank.activeRoots =
                                if (nextInFix.rank === currRank && nextInFix.node.isActiveRoot())
                                    nextInFix
                                else null
                        }
                    }
                } else {
                    currRank.loss?.let {
                        if (it === rankFix) {
                            currRank.loss =
                                if (nextInFix.rank === currRank && nextInFix.node.isPassive())
                                    nextInFix
                                else null
                        }
                    }
                }
                rankFix.rank = newRank
            }
            else -> {
                throw IllegalArgumentException("Unknown record type")
            }
        }
    }

    fun setActive(activeRecord: ActiveRecord, zeroRank: RankListRecord<T>) {
        active = activeRecord
        ++active!!.refCount
        loss = 0u
        rank = zeroRank
        ++zeroRank.refCount
    }

    fun setActiveRoot() {
        val currRank = getRank()
        val xFix = FixListRecord(this, currRank)
        rank = xFix
        currRank.activeRoots?.let { currRank.activeRoots = xFix }
    }

    fun setPassive() {
        --active!!.refCount
        active = null

        when (rank) {
            is RankListRecord<*> -> {
                val rankRecord = rank as RankListRecord<T>
                --rankRecord.refCount
            }
            is FixListRecord<*> -> {
                val fixRecord = rank as FixListRecord<T>
                --fixRecord.rank.refCount
            }
        }
        rank = null
    }

    fun setLoss(newLoss: UInt) {
        if (loss!! < newLoss) {
            val xFix = rank as FixListRecord<T>
            xFix.rank.loss?.let { xFix.rank.loss = xFix }
        }
        loss = newLoss
    }
}
