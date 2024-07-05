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

    fun increaseRank(heapRecord: HeapRecord<T>) {
        val currRank = getRank()
        val nextRank: RankListRecord<T>
        if (currRank.inc == null) {
            nextRank = RankListRecord(currRank.rankNumber + 1)
            currRank.inc = nextRank
            nextRank.dec = currRank
        } else nextRank = currRank.inc!!
        changeRank(nextRank, heapRecord)
    }

    fun decreaseRank(heapRecord: HeapRecord<T>) {
        val currRank = getRank()
        val prevRank = currRank.dec!!
        changeRank(prevRank, heapRecord)
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
                throw IllegalArgumentException("Rank pointer isn't pointing to a valid type")
            }
        }
    }

    fun changeRank(newRank: RankListRecord<T>, heapRecord: HeapRecord<T>) {
        removeRank(keepLoss = true, heapRecord)
        ++newRank.refCount

        if (isActiveRoot() || isActive() && loss != null && loss!! > 0u) {
            rank = FixListRecord(this, newRank)
            if (isActiveRoot()) heapRecord.fixListUpdatePositionActiveRoot(rank as FixListRecord<T>)
            else heapRecord.fixListUpdatePositionPositiveLoss(rank as FixListRecord<T>)
        } else {
            rank = newRank
        }
    }

    fun setRankToRankListRecord() {
        rank = getRank()
    }

    fun removeRank(keepLoss: Boolean, heapRecord: HeapRecord<T>) {
        val currRank = getRank()
        currRank.refCount--

        if (rank is FixListRecord<*>) heapRecord.fixListRemove(rank as FixListRecord<T>)
        rank = null
        if (!keepLoss) loss = null
    }

    fun setActiveFromPassive(heapRecord: HeapRecord<T>) {
        active = heapRecord.activeRecord
        ++active!!.refCount
        loss = 0u
        val zeroRank = heapRecord.rankList
        if (parent != null && parent!!.isPassive()) {
            // this becomes an active root
            val xFix = FixListRecord(this, zeroRank)
            rank = xFix
            heapRecord.fixListUpdatePositionActiveRoot(xFix)
        } else {
            rank = zeroRank
        }
        ++zeroRank.refCount
    }

    fun setActiveRootFromActive(heapRecord: HeapRecord<T>) {
        if (loss!! > 0u) {
            // if this is in fix-list as an active node with positive loss, remove it
            heapRecord.fixListRemove(rank as FixListRecord<T>)
        } else {
            if (rank is FixListRecord<*>)
                throw IllegalArgumentException(
                    "Trying to set an active root to active root. Node key: $item")
        }
        // set loss to zero and create a new fix-list record
        loss = 0u
        rank = FixListRecord(this, getRank())
        // insert the new fix-list node onto the fix-list
        heapRecord.fixListUpdatePositionActiveRoot(rank as FixListRecord<T>)
    }

    fun setPassive(heapRecord: HeapRecord<T>) {
        --active!!.refCount
        active = null

        removeRank(keepLoss = false, heapRecord)
        if (rank is FixListRecord<*>) heapRecord.fixListRemove(rank as FixListRecord<T>)

        // all active children become active roots
        leftChild?.forEach { currentChild ->
            if (currentChild.isActive()) currentChild.setActiveRootFromActive(heapRecord)
        }
    }

    fun setLoss(newLoss: UInt, heapRecord: HeapRecord<T>) {
        if (isPassive()) throw IllegalStateException("Only active nodes can have loss")
        if (isActiveRoot() && newLoss != 0u)
            throw IllegalStateException("Active roots are not allowed loss greater than 0")
        loss = newLoss
        if (newLoss == 0u && !isActiveRoot()) {
            // if not active root, remove from fix-list if present
            if (rank is FixListRecord<*>) heapRecord.fixListRemove(rank as FixListRecord<T>)
        } else if (newLoss > 0u) {
            // update fix-list
            if (rank is RankListRecord<*>) {
                val currRank = getRank()
                rank = FixListRecord(this, currRank)
                currRank.setLossPointer(rank as FixListRecord<T>)
            }
            val xFix = rank as FixListRecord<T>
            heapRecord.fixListUpdatePositionPositiveLoss(xFix)
        }
    }

    // demote an active root to being just active
    fun demoteActiveRoot(heapRecord: HeapRecord<T>) {
        val xFix = rank as FixListRecord<T>
        xFix.rank.removeFixListPointers(xFix)
        heapRecord.fixListRemove(xFix)
    }

    fun forEach(action: (NodeRecord<T>) -> Unit) {
        val visited = mutableSetOf<NodeRecord<T>>()
        var current: NodeRecord<T>? = this

        while (current !in visited) {
            val next = current!!.right!!
            visited.add(current)
            action(current)
            current = next
        }
    }
}
