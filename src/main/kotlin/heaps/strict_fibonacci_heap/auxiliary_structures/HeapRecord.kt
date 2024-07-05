package heaps.strict_fibonacci_heap.auxiliary_structures

class HeapRecord<T : Comparable<T>>(root: T? = null) {
    var size: Int = 0
    var root: NodeRecord<T>? = null
    var activeRecord: ActiveRecord = ActiveRecord()
    var nonLinkableChild: NodeRecord<T>? = null
    var qHead: NodeRecord<T>? = null
    var rankList: RankListRecord<T> = RankListRecord(0)

    var fixList: FixListRecord<T>? = null
        private set

    var singles: FixListRecord<T>? = null
        private set

    init {
        root?.let {
            val newNode = NodeRecord(root)
            this.root = newNode
            size = 1
        }
    }

    private fun setSingles(xFix: FixListRecord<T>) {
        if (singles == null && !xFix.node.isActiveRoot()) {
            singles = xFix
            return
        }
    }

    private fun removeSingles(xFix: FixListRecord<T>) {
        if (singles === xFix) {
            // try to look a new candidate for singles
            val nextInFix = xFix.right!!
            singles = if (nextInFix.isInPartThree()) nextInFix else null
        }
    }

    private fun fixListCut(xFix: FixListRecord<T>) {
        if (xFix.left == null || xFix.right == null) return
        val a = xFix.left!!
        val b = xFix.right!!

        // update singles if necessary
        removeSingles(xFix)

        when {
            // case 1: there is only one item in the fix-list
            a === xFix && xFix === b -> {
                fixList = null
            }

            // case 2: there are exactly two items in the fix-list
            a === b -> {
                fixList = a
                a.left = a
                a.right = a
            }

            // case 3: there are more than two items in the fix-list
            else -> {
                a.right = b
                b.left = a
                // if heapRecord's fixList pointer points to x, move it to the left
                if (fixList === xFix) fixList = a
            }
        }
        xFix.left = null
        xFix.right = null
    }

    fun fixListRemove(xFix: FixListRecord<T>) {
        fixListCut(xFix)

        // update the rank's fix-list pointers if necessary
        xFix.rank.removeFixListPointers(xFix)

        if (xFix.node.isActive()) {
            // if the node is active, change its rank pointer to the RankListRecord pointer
            xFix.node.setRankToRankListRecord()
        } else {
            // if the node is passive, just remove the rank
            xFix.node.removeRank(keepLoss = false, this)
        }
    }

    private fun fixListPutBetween(
        xFix: FixListRecord<T>,
        left: FixListRecord<T>,
        right: FixListRecord<T>
    ) {
        if (left === right) throw IllegalArgumentException("a and b need to be distinct")

        // cut xFix from the fix-list
        fixListCut(xFix)

        // paste x between new boundaries
        xFix.left = left
        xFix.right = right
        left.right = xFix
        right.left = xFix
    }

    private fun fixListInsertEmpty(xFix: FixListRecord<T>) {
        if (fixList != null)
            throw IllegalStateException(
                "Trying to perform the empty fix-list insertion with a non-empty fix-list")
        fixList = xFix
        xFix.left = xFix
        xFix.right = xFix
        setSingles(xFix)
    }

    private fun fixListMoveRightOf(xFix: FixListRecord<T>, yFix: FixListRecord<T>) {
        if (yFix.right === xFix) return

        val nextInFix = yFix.right!!

        if (yFix === nextInFix) {
            // there is only one item on the fix-list
            fixListMoveToTail(xFix)
        } else {
            fixListPutBetween(xFix = xFix, left = yFix, right = nextInFix)
        }
    }

    private fun fixListMoveLeftOf(xFix: FixListRecord<T>, yFix: FixListRecord<T>) {
        if (yFix.left === xFix) return

        val prevInFix = yFix.left!!

        if (yFix === prevInFix) {
            // there is only one item on the fix-list
            fixListMoveToHead(xFix)
        } else {
            fixListPutBetween(xFix = xFix, left = prevInFix, right = yFix)
        }
    }

    private fun fixListMoveToHead(xFix: FixListRecord<T>) {
        // check if fix-list is empty
        if (this.fixList == null) {
            fixListInsertEmpty(xFix)
            return
        }

        val firstInFix = this.fixList!!.right

        // check if xFix is already at the head of the fix-list
        if (firstInFix === xFix) return

        // remove xFix from the fix-list
        fixListCut(xFix)

        val lastInFix = this.fixList!!

        if (firstInFix === lastInFix) {
            // there is only one item left on the fix-list
            xFix.left = lastInFix
            xFix.right = lastInFix
            lastInFix.left = xFix
            lastInFix.right = xFix
        } else {
            fixListPutBetween(xFix = xFix, left = lastInFix, right = firstInFix!!)
        }
    }

    private fun fixListMoveToTail(xFix: FixListRecord<T>) {
        // check if xFix is already at the tail of the fix-list
        if (this.fixList === xFix) return

        // move xFix to the head of the list
        fixListMoveToHead(xFix)

        // move the tail pointer to the head
        this.fixList = xFix
    }

    fun fixListMoveNextToRankActiveRoots(xFix: FixListRecord<T>) {
        val activeRoots =
            xFix.rank.activeRoots
                ?: throw IllegalArgumentException(
                    "Trying to move an active root next to the rank's activeRoots pointer, but activeRoots is null")
        if (activeRoots === xFix)
            throw IllegalArgumentException(
                "Only use this method to put an active root on the fix-list next to its rank's activeRoots pointer")

        // if active roots is not in part one already, move it to the head of the fix-list
        if (!activeRoots.isInPartOne()) fixListMoveToHead(activeRoots)

        fixListMoveRightOf(xFix, activeRoots)
    }

    fun fixListMoveSingleActiveRoot(xFix: FixListRecord<T>) {
        if (xFix.rank.activeRoots !== xFix || xFix.rank.isActiveRootTransformable())
            throw IllegalArgumentException(
                "Only use this method to move an active root on part two of the fix-list when it is the only active root for its rank")
        if (xFix.isInPartTwo()) return

        // if the fix-list is empty, just insert it
        if (this.fixList == null) fixListInsertEmpty(xFix)
        else if (singles != null) fixListMoveLeftOf(xFix = xFix, yFix = singles!!)
        else {
            if (fixList!!.node.isActiveRoot()) {
                // if fix-list tail points to an active root, then parts 3 and 4 are empty
                fixListMoveToTail(xFix)
            } else {
                // part 3 is empty
                TODO()
            }
        }
    }

    // precondition: xFix.rank.loss !== xFix
    fun fixListMoveNextToRankLoss(xFix: FixListRecord<T>) {
        val loss =
            xFix.rank.loss
                ?: throw IllegalArgumentException(
                    "Trying to move a node with positive loss next to the the rank's loss pointer, but loss pointer is null")
        if (loss === xFix)
            throw IllegalArgumentException(
                "Only use this method to put a node with positive loss on the fix-list next to its rank's loss pointer")

        // if loss is not already in part four, move it to the tail of the fix-list
        if (!loss.isInPartFour()) fixListMoveToTail(loss)

        fixListMoveLeftOf(xFix, loss)
    }

    fun fixListMoveToSingleLoss(xFix: FixListRecord<T>) {
        if (xFix.rank.loss!! !== xFix || xFix.rank.isLossTransformable())
            throw IllegalArgumentException(
                "Only use this method to move a node with positive loss on part three of the fix-list when it is the only node with positive loss of its rank")
        if (xFix.isInPartThree()) return

        if (fixList == null) {
            // if the fix-list is empty, just insert it
            fixListInsertEmpty(xFix)
        } else if (singles != null) {
            fixListMoveRightOf(xFix, singles!!)
        } else {
            if (fixList!!.node.isActiveRoot()) {
                // if fix-list tail points to an active root, then parts 3 and 4 are empty
                fixListMoveToTail(xFix)
                setSingles(xFix)
            } else {
                TODO()
            }
        }
    }

    fun fixListUpdatePositionActiveRoot(xFix: FixListRecord<T>) {
        fixListCut(xFix)
        xFix.rank.setActiveRootsPointer(xFix)
        if (xFix.rank.activeRoots === xFix) fixListMoveSingleActiveRoot(xFix)
        else fixListMoveNextToRankActiveRoots(xFix)
    }

    fun fixListUpdatePositionPositiveLoss(xFix: FixListRecord<T>) {
        fixListCut(xFix)
        xFix.rank.setLossPointer(xFix)
        if (xFix.rank.loss === xFix) fixListMoveToSingleLoss(xFix)
        else fixListMoveNextToRankLoss(xFix)
    }
}
