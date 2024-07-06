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

    fun removeActiveRootsPointer(xFix: FixListRecord<T>, heapRecord: HeapRecord<T>) {
        if (activeRoots === xFix) {
            // try to select a new candidate for activeRoots
            val nextInFix = xFix.right!!
            activeRoots =
                if (nextInFix !== xFix &&
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

    fun removeLossPointer(xFix: FixListRecord<T>, heapRecord: HeapRecord<T>) {
        if (loss === xFix) {
            // try to select a new candidate for loss
            val prevInFix = xFix.left!!
            loss =
                if (prevInFix !== xFix &&
                    !prevInFix.node.isActiveRoot() &&
                    prevInFix.rank === xFix.rank) {
                    prevInFix
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

        val firstLoss = loss!!

        if (firstLoss.left == null || firstLoss.right == null) return false

        if (firstLoss.node.loss!! >= 2u) return true

        val secondLoss = loss!!.left!!
        return secondLoss.rank === loss!!.rank
    }

    fun isActiveRootTransformable(): Boolean {
        if (activeRoots == null) return false
        val firstActiveRoot = activeRoots!!

        if (activeRoots!!.left == null || activeRoots!!.right == null) return false

        val nextInFix = firstActiveRoot.right!!
        return nextInFix.node.isActiveRoot() && nextInFix.rank === this
    }
}
