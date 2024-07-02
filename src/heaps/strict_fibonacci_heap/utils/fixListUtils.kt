@file:Suppress("UNCHECKED_CAST")

package heaps.strict_fibonacci_heap.utils

import heaps.strict_fibonacci_heap.auxiliary_structures.FixListRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord

fun <T : Comparable<T>> fixListCut(x: FixListRecord<T>, heapRecord: HeapRecord<T>) {
    if (x.left == null || x.right == null) return
    val a = x.left!!
    val b = x.right!!

    // update singles if necessary
    if (heapRecord.singles === x) heapRecord.singles = if (!b.node.isActiveRoot()) b else null

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
            // if heapRecord's fixList pointer points to x, move it to the left
            if (heapRecord.fixList === x) heapRecord.fixList = a
        }
    }
}

fun <T : Comparable<T>> fixListRemove(x: FixListRecord<T>, heapRecord: HeapRecord<T>) {
    fixListCut(x, heapRecord)

    if (x.node.isActive()) x.node.setRank(x.rank) else x.node.setRank(null)
}

fun <T : Comparable<T>> fixListMove(
    x: FixListRecord<T>,
    left: FixListRecord<T>,
    right: FixListRecord<T>,
    heapRecord: HeapRecord<T>
) {
    if (x === left || x === right) return

    // cut x from the list if it's on it
    fixListCut(x, heapRecord)

    // paste x between new boundaries
    x.left = left
    x.right = right
    left.right = x
    right.left = x
}

fun <T : Comparable<T>> fixListMoveToHead(x: FixListRecord<T>, heapRecord: HeapRecord<T>) {
    // cut x from the list if it's on it
    fixListCut(x, heapRecord)
    if (heapRecord.fixList == null) {
        heapRecord.fixList = x
    } else {
        val lastInFix = heapRecord.fixList!!
        val firstInFix = lastInFix.right!!
        fixListMove(x, lastInFix, firstInFix, heapRecord)
    }
}

fun <T : Comparable<T>> fixListMoveToTail(x: FixListRecord<T>, heapRecord: HeapRecord<T>) {
    fixListMoveToHead(x, heapRecord)
    heapRecord.fixList = x
}

// x is either a newly created active root or it needs adjusting after a transformation
fun <T : Comparable<T>> moveToActiveRoots(x: NodeRecord<T>, heapRecord: HeapRecord<T>) {
    if (!x.isActiveRoot()) throw IllegalArgumentException("Node is not an active root")
    val xFix = x.rank as FixListRecord<T>
    val xRank = xFix.rank
    val xRankActiveRoot = xRank.activeRoots

    if (heapRecord.fixList == null) {
        // if the fix list is empty, just point to xFix
        heapRecord.fixList = xFix
        xFix.left = xFix
        xFix.right = xFix
    } else if (xRankActiveRoot == null) {
        // if there are no active roots of this rank, put x in part 2
        xRank.activeRoots = xFix

        if (heapRecord.fixList!!.node.isActiveRoot()) {
            // if fixList points to an active root then parts 3 and 4 are empty, so put x at the end
            // of the fix-list and update the heap record's pointer
            fixListMoveToTail(xFix, heapRecord)
        } else {
            // use singles to reach part 2
            fixListMove(xFix, heapRecord.singles!!.left!!, heapRecord.singles!!, heapRecord)
        }
    } else {
        // if there is at least one active root of this rank
        val nextInFix = xRankActiveRoot.right!!
        if (nextInFix.node.isActiveRoot() && nextInFix.rank === xRankActiveRoot.rank) {
            // if the node to the right of nextInFix is also an active root of the same rank, it
            // means that xRankActiveRoot is already in part 1, so just put x in-between them
            fixListMove(xFix, xRankActiveRoot, nextInFix, heapRecord)
        } else {
            // otherwise nextInFix is in part 2, so move both to part 1
            fixListMoveToHead(xRankActiveRoot, heapRecord)
            fixListMove(xFix, xRankActiveRoot, xRankActiveRoot.right!!, heapRecord)
        }
    }
}

// x is an active node with positive loss or its loss has just increased
fun <T : Comparable<T>> moveToPositiveLoss(x: NodeRecord<T>, heapRecord: HeapRecord<T>) {
    val xFix = x.rank as FixListRecord<T>
    val xRank = xFix.rank
    val rankLoss = xRank.loss
    if (rankLoss == null) {
        // if there are no active nodes with positive loss for this rank, move to part 3
        xRank.loss = xFix
        fixListMove(xFix, heapRecord.singles!!, heapRecord.singles!!.right!!, heapRecord)
    } else {
        val nextInFix = rankLoss.right!!
        if (rankLoss.rank === nextInFix.rank) {
            // this rank is loss transformable, put x in-between
            fixListMove(xFix, rankLoss, nextInFix, heapRecord)
        } else {
            // rankLoss is in part 3, move both to part 4
            fixListMoveToTail(rankLoss, heapRecord)
            fixListMove(xFix, rankLoss, rankLoss.right!!, heapRecord)
        }
    }
}
