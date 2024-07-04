@file:Suppress("UNCHECKED_CAST")

package heaps.strict_fibonacci_heap.auxiliary_structures

import heaps.strict_fibonacci_heap.utils.fixListRemove
import heaps.strict_fibonacci_heap.utils.moveToActiveRoots
import heaps.strict_fibonacci_heap.utils.moveToPositiveLoss
import heaps.strict_fibonacci_heap.utils.updateRankPointersBeforeRemove

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

    fun increaseRank(heapRecord: HeapRecord<T>) {
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
        setRank(nextRank, heapRecord)
    }

    fun decreaseRank(heapRecord: HeapRecord<T>) {
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
        setRank(prevRank, heapRecord)
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

    fun setRank(newRank: RankListRecord<T>?, heapRecord: HeapRecord<T>) {
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
                updateRankPointersBeforeRemove(rankFix, heapRecord)
                rankFix.rank = newRank
            }
            else -> {
                throw IllegalArgumentException("Unknown record type")
            }
        }
    }

    fun setActiveFromPassive(
        activeRecord: ActiveRecord,
        zeroRank: RankListRecord<T>,
        heapRecord: HeapRecord<T>
    ) {
        active = activeRecord
        ++active!!.refCount
        loss = 0u
        if (parent != null && parent!!.isPassive()) {
            val xFix = FixListRecord(this, zeroRank)
            rank = xFix
            moveToActiveRoots(this, heapRecord)
            zeroRank.activeRoots?.let { zeroRank.activeRoots = xFix }
        } else {
            rank = zeroRank
        }
        ++zeroRank.refCount
    }

    fun setActiveRootFromActive() {
        val currRank = getRank()
        val xFix = FixListRecord(this, currRank)
        rank = xFix
        currRank.activeRoots?.let { currRank.activeRoots = xFix }
        loss = 0u
    }

    fun setPassive(heapRecord: HeapRecord<T>) {
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
                fixListRemove(fixRecord, heapRecord)
            }
        }
        rank = null
        loss = null
    }

    fun setLoss(newLoss: UInt, heapRecord: HeapRecord<T>) {
        if (isPassive()) throw IllegalStateException("Only active nodes can have loss")
        if (isActiveRoot() && newLoss != 0u)
            throw IllegalStateException("Active roots are not allowed loss greater than 0")
        if (newLoss == 0u && !isActiveRoot()) {
            // if not active root, remove from fix-list if present
            if (rank is FixListRecord<*>) fixListRemove(rank as FixListRecord<T>, heapRecord)
        } else if (newLoss > 0u) {
            // update fix-list if needed
            val xFix = rank as FixListRecord<T>
            moveToPositiveLoss(this, heapRecord)
            xFix.rank.loss?.let { xFix.rank.loss = xFix }
        }
        loss = newLoss
    }

    // demote an active root to being just active
    fun demoteActiveRoot(heapRecord: HeapRecord<T>) {
        val xFix = rank as FixListRecord<T>
        if (xFix.rank.activeRoots === xFix) {
            // if the node's rank's activeRoots pointer points to this, then update it
            val nextInFix = xFix.right!!
            if (nextInFix.rank === xFix.rank) xFix.rank.activeRoots = nextInFix
            else xFix.rank.activeRoots = null
        }
        fixListRemove(xFix, heapRecord)
    }
}
