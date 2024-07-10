@file:Suppress("UNCHECKED_CAST")

package heaps.strict_fibonacci_heap.auxiliary_structures

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Class which defines the actual node of the Strict Fibonacci Heap. It holds pointers to its
 * siblings, kept in a circular, doubly-linked list, its parent and its left child.
 */
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
        set(value) {
            when (value) {
                is RankListRecord<*>,
                is FixListRecord<*>,
                null -> field = value
                else -> throw IllegalArgumentException("Unsupported rank type")
            }
        }

    /**
     * Returns whether the node is an active root.
     *
     * T(n) = O(1)
     */
    fun isActiveRoot(): Boolean {
        return isActive() && (parent != null && !parent!!.isActive())
    }

    /**
     * Returns whether the node is active.
     *
     * T(n) = O(1)
     */
    fun isActive(): Boolean {
        return active?.flag ?: false
    }

    /**
     * Returns whether the node is passive.
     *
     * T(n) = O(1)
     */
    fun isPassive(): Boolean {
        return !isActive()
    }

    /**
     * Returns whether the node is passive and linkable.
     *
     * A node is linkable if it is passive and all of its children are also passive. Since active
     * nodes are always linked as the leftmost child, it's sufficient to check if the leftmost child
     * is passive
     *
     * T(n) = O(1)
     */
    fun isPassiveLinkable(): Boolean {
        if (leftChild != null && leftChild!!.isActive()) return false
        return isPassive()
    }

    /**
     * Increases the node's rank by one.
     *
     * T(n) = O(1)
     */
    fun increaseRank(heapRecord: HeapRecord<T>) {
        logger.debug {
            "Increasing rank from ${getRank().rankNumber} to ${getRank().rankNumber +1} for node $item"
        }
        val currRank = getRank()
        val nextRank: RankListRecord<T>
        if (currRank.inc == null) {
            nextRank = RankListRecord(currRank.rankNumber + 1)
            currRank.inc = nextRank
            nextRank.dec = currRank
        } else nextRank = currRank.inc!!
        changeRank(nextRank, heapRecord)
    }

    /**
     * Decreases the node's rank by one.
     *
     * T(n) = O(1)
     */
    fun decreaseRank(heapRecord: HeapRecord<T>) {
        logger.debug {
            "Decreasing rank from ${getRank().rankNumber} to ${getRank().rankNumber -1} for node $item"
        }
        val currRank = getRank()
        val prevRank = currRank.dec!!
        changeRank(prevRank, heapRecord)
    }

    /**
     * Returns the pointer to the rank. if [rank] is a [FixListRecord] then the output is the [rank]
     * field from the [FixListRecord]. Otherwise, it is simply the node's [rank] pointer.
     *
     * T(n) = O(1)
     */
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

    /**
     * Changes the node's rank. If it is on the fix-list, its position is updated
     *
     * T(n) = O(1)
     */
    private fun changeRank(newRank: RankListRecord<T>, heapRecord: HeapRecord<T>) {
        logger.debug {
            "Changing rank for node $item from ${getRank().rankNumber} to ${newRank.rankNumber}"
        }
        val isOnFixList = rank is FixListRecord<*>
        removeRank(keepLoss = true, heapRecord)
        rank = newRank
        ++newRank.refCount

        if (isOnFixList) heapRecord.insertIntoFixList(this)
    }

    /**
     * Removes the node's rank. If it is on the fix-list, it is also removed from it.
     *
     * T(n) = O(1)
     */
    private fun removeRank(keepLoss: Boolean, heapRecord: HeapRecord<T>) {
        logger.debug { "Removing rank for node $item" }
        if (rank == null) return
        val currRank = getRank()
        currRank.refCount--

        if (rank is FixListRecord<*>) heapRecord.fixListRemove(rank as FixListRecord<T>, true)
        rank = null
        if (!keepLoss) loss = null
    }

    /**
     * Sets the currently passive node to active
     *
     * T(n) = θ(log n)
     */
    fun setActiveFromPassive(heapRecord: HeapRecord<T>) {
        logger.debug { "Setting passive node $item to active" }
        active = heapRecord.activeRecord
        ++active!!.refCount
        loss = 0u
        val zeroRank = heapRecord.rankList
        rank = zeroRank

        // this becomes an active root
        if (parent != null && parent!!.isPassive()) heapRecord.insertIntoFixList(this)

        ++zeroRank.refCount

        // demote all active root children to active
        leftChild?.forEach {
            if (it.rank is FixListRecord<*> && it.loss == 0u) {
                it.demoteActiveRoot(heapRecord)
                return@forEach true
            }
            return@forEach false
        }
    }

    /**
     * Sets the currently active root node to active
     *
     * T(n) = O(1)
     */
    fun setActiveRootFromActive(heapRecord: HeapRecord<T>) {
        logger.debug { "Setting active node $item to active root" }
        // if this is in fix-list as an active node with positive loss, remove it
        if (rank is FixListRecord<*>) heapRecord.fixListRemove(rank as FixListRecord<T>, true)

        // set loss to zero and insert it again onto the fix-list
        loss = 0u
        heapRecord.insertIntoFixList(this)
    }

    /**
     * Sets the currently active node to passive
     *
     * T(n) = θ(log n)
     */
    fun setPassive(heapRecord: HeapRecord<T>) {
        logger.debug { "Setting active node $item to passive" }
        --active!!.refCount
        active = null

        removeRank(keepLoss = false, heapRecord)

        // all active children become active roots
        leftChild?.forEach { currentChild ->
            if (currentChild.isActive()) {
                currentChild.setActiveRootFromActive(heapRecord)
                return@forEach true
            }
            return@forEach false
        }
    }

    /**
     * Sets the node's loss
     *
     * T(n) = O(1)
     */
    fun setLoss(newLoss: UInt, heapRecord: HeapRecord<T>) {
        logger.debug { "Setting loss at $newLoss for node $item" }
        if (isPassive()) throw IllegalStateException("Only active nodes can have loss")
        if (isActiveRoot() && newLoss != 0u)
            throw IllegalStateException("Active roots are not allowed loss greater than 0")
        loss = newLoss
        if (newLoss == 0u && !isActiveRoot()) {
            // if not active root, remove from fix-list if present
            if (rank is FixListRecord<*>) heapRecord.fixListRemove(rank as FixListRecord<T>, true)
        } else if (newLoss > 0u) {
            // update fix-list
            if (rank is FixListRecord<*>) {
                heapRecord.fixListRemove(rank as FixListRecord<T>, true)
            }
            heapRecord.insertIntoFixList(this)
        }
    }

    /**
     * Demote an active root to being just active
     *
     * T(n) = O(1)
     */
    fun demoteActiveRoot(heapRecord: HeapRecord<T>) {
        logger.debug { "Setting active root $item to active" }
        heapRecord.fixListRemove(rank as FixListRecord<T>, true)
    }

    /**
     * Visits the current node and its siblings left to right. At each visit, it applies the action
     * to the current node. If the action returns false, then stop early
     *
     * T(n) = O(log n)
     */
    fun forEach(action: (NodeRecord<T>) -> Boolean) {
        val visited = mutableSetOf<NodeRecord<T>>()
        var current: NodeRecord<T>? = this

        while (current !in visited) {
            val next = current!!.right!!
            visited.add(current)
            if (!action(current)) break
            current = next
        }
    }
}
