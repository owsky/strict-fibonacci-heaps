package heaps.strict_fibonacci_heap.utils

import heaps.strict_fibonacci_heap.auxiliary_structures.FixListRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord

fun <T : Comparable<T>> fixListCut(x: FixListRecord<T>, heapRecord: HeapRecord<T>) {
    val a = x.left
    val b = x.right
    when {
        // case 1: there is only one item in the fix-list
        a === x && x === b -> {
            heapRecord.fixList = null
        }

        // case 2: there are exactly two items in the fix-list
        a === b -> {
            heapRecord.fixList = a
            a.left = a
            a.right = a
        }

        // case 3: there are more than two items in the fix-list
        else -> {
            a.right = b
            b.left = a
        }
    }
}

fun <T : Comparable<T>> fixListRemove(x: FixListRecord<T>, heapRecord: HeapRecord<T>) {
    val a = x.left
    val b = x.right
    if (heapRecord.singles === x) {
        heapRecord.singles = if (!b.node.isActiveRoot()) b else null
    }

    fixListCut(x, heapRecord)

    val xRank = x.node.getRank()
    --xRank.refCount

    x.node.rankFixListRecord = null
    x.node.rankRankListRecord = null
}

fun <T : Comparable<T>> fixListMove(
    x: FixListRecord<T>,
    left: FixListRecord<T>,
    right: FixListRecord<T>,
    heapRecord: HeapRecord<T>
) {
    // cut x from the list
    fixListCut(x, heapRecord)

    // paste x between new boundaries
    x.left = left
    x.right = right
    left.right = x
    right.left = x
}

// x is an active root in part 2 or has just become an active root
fun <T : Comparable<T>> moveToActiveRoots(x: NodeRecord<T>, heapRecord: HeapRecord<T>) {
    if (x.rankFixListRecord == null) {
        x.rankFixListRecord = FixListRecord(x, x.rankRankListRecord!!)
    }
    val xFix = x.rankFixListRecord!!
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
            fixListMove(xFix, heapRecord.fixList!!, heapRecord.fixList!!.right, heapRecord)
            heapRecord.fixList = xFix
        } else {
            // use singles to reach part 2
            fixListMove(xFix, heapRecord.singles!!.left, heapRecord.singles!!, heapRecord)
        }
    } else {
        // if there is at least one active root of this rank
        val nextInFix = xFix.right
        if (nextInFix.node.isActiveRoot()) {
            // if next is also an active root
            if (nextInFix.rank === xRank) {
                // if next active root has the same rank, put x in-between
                fixListMove(xFix, xRankActiveRoot, nextInFix, heapRecord)
            } else {
                // if there is only one active root of this rank, move both to part 1
                fixListMove(
                    xRankActiveRoot, heapRecord.fixList!!, heapRecord.fixList!!.right, heapRecord)
                fixListMove(xFix, xRankActiveRoot, xRankActiveRoot.right, heapRecord)
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
