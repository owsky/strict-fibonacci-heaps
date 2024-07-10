package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.utils.*

/**
 * Link transformation.
 *
 * Link a node [x] and its subtree below another node [y], by removing [x] from the child list of
 * its current parent and making [x] a child of [y]. If [x] is active it is made the leftmost child
 * of [y]; if [x] is passive it is made the rightmost child of [y].
 *
 * T(n) = O(1)
 */
fun <T : Comparable<T>> link(x: NodeRecord<T>, y: NodeRecord<T>, heapRecord: HeapRecord<T>) {
    val previousParent = x.parent
    val xIsActiveRoot = x.isActiveRoot()
    val xRight = x.right
    val xLeft = x.left
    if (x.parent === y) return

    x.parent = y

    previousParent?.let {
        if (it.leftChild === x) {
            val nextChild = it.leftChild!!.right
            previousParent.leftChild = if (nextChild === x) null else nextChild
        }
    }

    if (y.parent === x) {
        // if x is being linked to one of its children y, first link y to the parent of x
        link(y, previousParent!!, heapRecord)
    }

    // if x is the non-linkable child of the root, update the pointer
    if (heapRecord.nonLinkableChild === x) {
        if (x.isActive()) {
            // if x is active, then it's the right-most active node and there are no non-linkable
            // passive nodes, so shift the pointer left is possible
            heapRecord.nonLinkableChild =
                if (xLeft !== x && !xLeft!!.isPassiveLinkable()) xLeft else null
        } else {
            // if x is passive and non-linkable, shift the pointer right if possible
            heapRecord.nonLinkableChild =
                if (xRight !== x && !xRight!!.isPassiveLinkable()) xRight else null
        }
    }

    // remove x from siblings
    removeFromSiblings(x)

    // if previousParent is the non-linkable child and x's right sibling is passive, then if
    // previousParent is passive it becomes linkable
    previousParent?.let {
        if (previousParent === heapRecord.nonLinkableChild && previousParent.isPassive()) {
            if ((xRight === x) || (xRight!!.isPassive())) {
                heapRecord.nonLinkableChild =
                    if (previousParent.left !== previousParent &&
                        !previousParent.left!!.isPassiveLinkable())
                        previousParent.left
                    else null
            }
        }
    }

    previousParent?.let {
        // if both x and its previous parent are active, decrease the parent rank
        if (x.isActive() && it.isActive()) it.decreaseRank(heapRecord)
        // if the previous parent is active but not an active root, increase its loss
        if (it.isActive() && !it.isActiveRoot()) it.setLoss(it.loss!! + 1u, heapRecord)
    }

    // if x is an active root and y is active, x ceases to be an active root
    if (xIsActiveRoot && y.isActive()) x.demoteActiveRoot(heapRecord)

    // if both x and y are active, increase the rank of y
    if (x.isActive() && y.isActive()) y.increaseRank(heapRecord)

    // if x is not an active root, but it is active and y is passive, then x becomes an active root
    if (!xIsActiveRoot && x.isActive() && y.isPassive()) x.setActiveRootFromActive(heapRecord)

    // insert x into y's children
    if (y.leftChild == null) {
        // if y has no children
        y.leftChild = x
        x.left = x
        x.right = x
        if (y === heapRecord.root) updateNonLinkableChild(x, heapRecord)
    } else {
        // if y has at least one child
        if (y === heapRecord.root) {
            if (x.isActive()) {
                // if x is active, add it as leftmost child and possibly update the non-linkable
                // child pointer
                insertAsFirstSibling(x, y)
                updateNonLinkableChild(x, heapRecord)
            } else if (y.isPassiveLinkable()) {
                // if x is passive and linkable, add it as the rightmost child
                insertAsLastSibling(x, y)
            } else {
                // if x is passive and non-linkable
                if (heapRecord.nonLinkableChild == null) {
                    // if non-linkable child is null then the root only has passive and linkable
                    // children, thus set the pointer to x and add x as left-most child
                    updateNonLinkableChild(x, heapRecord)
                    insertAsFirstSibling(x, y)
                } else {
                    // otherwise simply add x to the right of the non-linkable child, and possibly
                    // update it
                    heapRecord.nonLinkableChild!!
                    heapRecord.nonLinkableChild!!.right!!
                    insertNodeBetween(
                        heapRecord.nonLinkableChild!!, x, heapRecord.nonLinkableChild!!.right!!)
                    updateNonLinkableChild(x, heapRecord)
                }
            }
        } else {
            if (x.isActive()) insertAsFirstSibling(x, y) else insertAsLastSibling(x, y)
        }
    }
}
