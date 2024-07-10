package heaps.strict_fibonacci_heap.auxiliary_structures

/**
 * This class defines the node to be placed in the rank list, which is a doubly-linked list.
 *
 * [rankNumber] is only stored for debugging purposes.
 */
class RankListRecord<T : Comparable<T>>(val rankNumber: Int) {
    var inc: RankListRecord<T>? = null
    var dec: RankListRecord<T>? = null
    var refCount: Int = 0

    var activeRoots: FixListRecord<T>? = null
        private set

    var loss: FixListRecord<T>? = null
        private set

    /**
     * Sets the rank's [activeRoots] pointer.
     *
     * T(n) = O(1)
     */
    fun setActiveRootsPointer(xFix: FixListRecord<T>) {
        if (activeRoots == null) {
            activeRoots = xFix
            return
        }
    }

    /**
     * Resets the rank's [activeRoots] pointer.
     *
     * T(n) = O(1)
     */
    private fun resetActiveRootsPointer(xFix: FixListRecord<T>) {
        if (activeRoots === xFix) {
            // try to select a new candidate for activeRoots
            val nextInFix = xFix.right
            activeRoots =
                if (nextInFix != null &&
                    nextInFix.node.isActiveRoot() &&
                    nextInFix.rank === xFix.rank) {
                    nextInFix
                } else {
                    null
                }
        }
    }

    /**
     * Sets the rank's [loss] pointer.
     *
     * T(n) = O(1)
     */
    fun setLossPointer(xFix: FixListRecord<T>) {
        if (loss == null) {
            loss = xFix
            return
        }
    }

    /**
     * Resets the rank's [loss] pointer.
     *
     * T(n) = O(1)
     */
    private fun resetLossPointer(xFix: FixListRecord<T>) {
        if (loss === xFix) {
            // try to select a new candidate for loss
            val nextInFix = xFix.right
            loss =
                if (nextInFix !== null &&
                    !nextInFix.node.isActiveRoot() &&
                    nextInFix.rank === xFix.rank) {
                    nextInFix
                } else {
                    null
                }
        }
    }

    /**
     * Resets the rank's [activeRoots] and [loss] pointers.
     *
     * T(n) = O(1)
     */
    fun resetFixListPointers(xFix: FixListRecord<T>) {
        resetActiveRootsPointer(xFix)
        resetLossPointer(xFix)
    }

    /**
     * Returns whether the rank is loss-transformable.
     *
     * T(n) = O(1)
     */
    fun isLossTransformable(): Boolean {
        if (loss == null) return false

        loss?.let {
            if (it.node.loss!! >= 2u) return true
            it.right?.let { nextInFix ->
                return nextInFix.rank === this
            }
        }

        return false
    }

    /**
     * Returns whether the rank is active-roots-transformable.
     *
     * T(n) = O(1)
     */
    fun isActiveRootTransformable(): Boolean {
        if (activeRoots == null) return false

        activeRoots?.right?.let { nextInFix ->
            return nextInFix.rank === this
        } ?: return false
    }
}
