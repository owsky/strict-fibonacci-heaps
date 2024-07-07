package heaps.strict_fibonacci_heap.auxiliary_structures

class RankListRecord<T : Comparable<T>>(val rankNumber: Int) {
    var inc: RankListRecord<T>? = null
    var dec: RankListRecord<T>? = null
    var refCount: Int = 0

    var activeRoots: FixListRecord<T>? = null
        private set

    var loss: FixListRecord<T>? = null
        private set

    fun setActiveRootsPointer(xFix: FixListRecord<T>) {
        if (activeRoots == null) {
            activeRoots = xFix
            return
        }
    }

    private fun removeActiveRootsPointer(xFix: FixListRecord<T>, heapRecord: HeapRecord<T>) {
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

    fun setLossPointer(xFix: FixListRecord<T>) {
        if (loss == null) {
            loss = xFix
            return
        }
    }

    private fun removeLossPointer(xFix: FixListRecord<T>, heapRecord: HeapRecord<T>) {
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

    fun removeFixListPointers(xFix: FixListRecord<T>, heapRecord: HeapRecord<T>) {
        removeActiveRootsPointer(xFix, heapRecord)
        removeLossPointer(xFix, heapRecord)
    }

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

    fun isActiveRootTransformable(): Boolean {
        if (activeRoots == null) return false

        activeRoots?.right?.let { nextInFix ->
            return nextInFix.rank === this
        } ?: return false
    }
}
