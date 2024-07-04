@file:Suppress("UNCHECKED_CAST")

package heaps.strict_fibonacci_heap.utils

import heaps.strict_fibonacci_heap.auxiliary_structures.FixListRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord

fun <T : Comparable<T>> updateRankPointersBeforeRemove(
    x: FixListRecord<T>,
    heapRecord: HeapRecord<T>
) {
    if (x.rank.activeRoots === x) {
        val nextInFix = x.right!!
        if (nextInFix !== x && nextInFix.node.isActiveRoot() && nextInFix.rank === x.rank) {
            // if nextInFix is an active root of the same rank, then both x and nextInFix are in
            // part 1
            x.rank.activeRoots = nextInFix
            if (nextInFix.right!! === x ||
                !nextInFix.right!!.node.isActiveRoot() ||
                nextInFix.right!!.rank !== x.rank) {
                // if nextInFix is the last active root of this rank, it needs to be moved to part 2
                moveToActiveRoots(nextInFix.node, heapRecord)
            }
        } else {
            x.rank.activeRoots = null
        }
    } else if (x.rank.loss === x) {
        val prevInFix = x.left!!
        if (prevInFix !== x && !prevInFix.node.isActiveRoot() && prevInFix.rank === x.rank) {
            // the rank is loss transformable thus x and prevInFix are in part 4
            x.rank.loss = prevInFix
            if (prevInFix.node.loss!! < 2u &&
                prevInFix.left!! !== x &&
                (prevInFix.left!!.rank !== x.rank || prevInFix.left!!.node.isActiveRoot())) {
                // if prevInFix has loss less than 2 and the node to its left is not a node with
                // positive loss of the same rank, move prevInFix to part 3
                moveToPositiveLoss(prevInFix.node, heapRecord)
            }
        } else {
            x.rank.loss = null
        }
    }
}

fun <T : Comparable<T>> fixListCut(x: FixListRecord<T>, heapRecord: HeapRecord<T>) {
    if (x.left == null || x.right == null) return
    val a = x.left!!
    val b = x.right!!

    // update singles if necessary
    if (heapRecord.singles === x) heapRecord.singles = if (!b.node.isActiveRoot()) b else null

    // update rank's activeRoots or loss pointer if necessary
    updateRankPointersBeforeRemove(x, heapRecord)

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
    x.left = null
    x.right = null
}

fun <T : Comparable<T>> fixListRemove(x: FixListRecord<T>, heapRecord: HeapRecord<T>) {
    fixListCut(x, heapRecord)

    if (x.node.isActive()) x.node.setRank(x.rank, heapRecord) else x.node.setRank(null, heapRecord)
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
        x.left = x
        x.right = x
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
        xRank.activeRoots = xFix
    } else if (xRankActiveRoot == null) {
        // if there are no active roots of this rank, put x in part 2
        xRank.activeRoots = xFix

        if (heapRecord.fixList!!.node.isActiveRoot()) {
            // if fixList points to an active root then parts 3 and 4 are empty, so put x at the end
            // of the fix-list and update the heap record's pointer
            fixListMoveToTail(xFix, heapRecord)
        } else {
            if (heapRecord.singles == null) {
                // if singles is null, then parts 3 and 4 are empty
                fixListMoveToTail(xFix, heapRecord)
            } else {
                // use singles to reach part 2
                fixListMove(xFix, heapRecord.singles!!.left!!, heapRecord.singles!!, heapRecord)
            }
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
    if (x.isPassive()) throw IllegalArgumentException("Node is not active")
    else if (x.loss == null || x.loss == 0u)
        throw IllegalArgumentException("Node doesn't have positive loss")
    val xFix = x.rank as FixListRecord<T>
    val xRank = xFix.rank
    val rankLoss = xRank.loss

    if (heapRecord.fixList == null) {
        // if the fix-list is empty, just add at the end
        fixListMoveToTail(xFix, heapRecord)
        // set the rank's loss pointer to xFix
        xRank.loss = xFix
        // set the heap record's singles pointer to xFix
        heapRecord.singles = xFix
    } else if (rankLoss == null) {
        // if there are no active nodes with positive loss for this rank, move to part 3
        xRank.loss = xFix

        if (heapRecord.singles == null) {
            // if singles is null, then parts 3 and 4 are empty
            heapRecord.singles = xFix
            fixListMoveToTail(xFix, heapRecord)
        } else {
            // if singles is not null, just put x to its right
            fixListMove(xFix, heapRecord.singles!!, heapRecord.singles!!.right!!, heapRecord)
        }
    } else {
        // if rankLoss is not null, then
        // if rankLoss is in part 3 then move it to part 4, then move x to its left
        // otherwise move x to its right
        val nextInFix = rankLoss.right!!
        if (rankLoss.rank === nextInFix.rank) {
            // this rank is loss transformable, put x in-between
            fixListMove(xFix, rankLoss, nextInFix, heapRecord)
        } else {
            // rankLoss is in part 3, move both to part 4
            fixListMoveToTail(rankLoss, heapRecord)
            fixListMove(xFix, rankLoss.left!!, rankLoss, heapRecord)
        }
    }
}
