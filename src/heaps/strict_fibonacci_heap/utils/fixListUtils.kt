package heaps.strict_fibonacci_heap.utils

import heaps.strict_fibonacci_heap.auxiliary_structures.FixListRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord

fun <T : Comparable<T>> fixListRemove(x: FixListRecord<T>, heapRecord: HeapRecord<T>) {
    val beforeX = x.left
    val afterX = x.right
    if (heapRecord.singles === x) heapRecord.singles = afterX
    beforeX.right = afterX
    afterX.left = beforeX
}

fun <T : Comparable<T>> fixListMove(
    x: FixListRecord<T>,
    left: FixListRecord<T>,
    right: FixListRecord<T>,
    heapRecord: HeapRecord<T>
) {
    // cut x from the list
    fixListRemove(x, heapRecord)

    // paste x between new boundaries
    x.left = left
    x.right = right
    left.right = x
    right.left = x
}

// x is an active root in part 2 or has just become an active root
fun <T : Comparable<T>> moveToActiveRoots(x: NodeRecord<T>, heapRecord: HeapRecord<T>) {
    val xFix = x.rankFixListRecord ?: FixListRecord(x, x.rankRankListRecord!!)
    val xRank = xFix.rank
    val xRankActiveRoot = xRank.activeRoots

    // if the fix list is empty, just point to xFix
    if (heapRecord.fixList == null) {
        heapRecord.fixList = xFix
    } else if (xRankActiveRoot == null) {
        // if there are no active roots of this rank, put in part 2
        xRank.activeRoots = xFix

        if (heapRecord.fixList!!.node.isActiveRoot()) {
            // if fix list points to an active root, parts 3 and 4 are empty
            fixListMove(
                x.rankFixListRecord!!, heapRecord.fixList!!.left, heapRecord.fixList!!, heapRecord)
        } else {
            // use singles to reach part 2
            fixListMove(
                x.rankFixListRecord!!, heapRecord.singles!!.left, heapRecord.singles!!, heapRecord)
        }
    } else {
        // if there is at least one active root of this rank
        val nextInFix = xFix.right
        if (nextInFix.node.isActiveRoot()) {
            // if next is also an active root
            if (nextInFix.rank === xRank) {
                // if next active root has the same rank, put x in-between
                fixListMove(x.rankFixListRecord!!, xRankActiveRoot, nextInFix, heapRecord)
            } else {
                // if there is only one active root of this rank, move both to part 1
                fixListMove(
                    xRankActiveRoot, heapRecord.fixList!!, heapRecord.fixList!!.right, heapRecord)
                fixListMove(
                    x.rankFixListRecord!!, xRankActiveRoot, xRankActiveRoot.right, heapRecord)
            }
        }
    }
}

// x is an active node with positive loss or its loss has just increased
fun <T : Comparable<T>> moveToPositiveLoss(x: NodeRecord<T>, heapRecord: HeapRecord<T>) {
    val xFix = x.rankFixListRecord ?: FixListRecord(x, x.rankRankListRecord!!)
    val xRank = xFix.rank
    val rankLoss = xRank.loss
    if (rankLoss == null) {
        // if there are no active nodes with positive loss for this rank, move to part 3
        xRank.loss = xFix
        fixListMove(xFix, heapRecord.singles!!, heapRecord.singles!!.right, heapRecord)
    } else {
        val nextInFix = rankLoss.right
        if (rankLoss.rank === nextInFix.rank) {
            // this rank is loss transformable, put x in-between
            fixListMove(xFix, rankLoss, nextInFix, heapRecord)
        } else {
            // rankLoss is in part 3, move both to part 4
            fixListMove(rankLoss, heapRecord.fixList!!, heapRecord.fixList!!.right, heapRecord)
            fixListMove(xFix, rankLoss, rankLoss.right, heapRecord)
        }
    }
}

fun <T : Comparable<T>> moveToRank(x: NodeRecord<T>, heapRecord: HeapRecord<T>) {
    if (x.isActiveRoot()) moveToActiveRoots(x, heapRecord) else moveToPositiveLoss(x, heapRecord)
}
