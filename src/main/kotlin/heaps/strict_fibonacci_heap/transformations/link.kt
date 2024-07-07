package heaps.strict_fibonacci_heap.transformations

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.utils.insertIntoCircularList
import heaps.strict_fibonacci_heap.utils.removeFromCircularList

fun <T : Comparable<T>> link(x: NodeRecord<T>, y: NodeRecord<T>, heapRecord: HeapRecord<T>) {
    val previousParent = x.parent
    val xIsActiveRoot = x.isActiveRoot()
    x.parent = y

    // if previous parent's left child is x, update its left child pointer
    previousParent?.let {
        if (it.leftChild === x) {
            val nextChild = it.leftChild!!.right
            previousParent.leftChild = if (nextChild === x) null else nextChild
        }
    }

    // check if nonLinkableChild needs updating
    if (y === heapRecord.root && heapRecord.nonLinkableChild == null && !x.isPassiveLinkable())
        heapRecord.nonLinkableChild = x
    else if (previousParent === heapRecord.root && heapRecord.nonLinkableChild === x) {
        val nextChild = x.right!!
        if (nextChild !== x && !nextChild.isPassiveLinkable())
            heapRecord.nonLinkableChild = nextChild
        else heapRecord.nonLinkableChild = null
    }

    // remove x from siblings
    removeFromCircularList(x)

    previousParent?.let {
        // if both x and its previous parent are active, decrease the parent rank
        if (x.isActive() && it.isActive()) it.decreaseRank(heapRecord)
        // if the previous parent is active but not an active root, increase its loss
        if (it.isActive() && !it.isActiveRoot()) it.setLoss(it.loss!! + 1u, heapRecord)
        // if previousParent is the non-linkable child, check if it still is
        if (previousParent === heapRecord.nonLinkableChild && previousParent.isPassiveLinkable()) {
            val nextSibling = previousParent.right!!
            heapRecord.nonLinkableChild =
                if (nextSibling !== previousParent && !nextSibling.isPassiveLinkable()) nextSibling
                else null
        }
    }

    // if x is an active root and y is active, x ceases to be an active root
    if (xIsActiveRoot && y.isActive()) {
        //
        x.demoteActiveRoot(heapRecord)
    }

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
    } else {
        // if y has at least one child
        val firstChild = y.leftChild!!
        val lastChild = firstChild.left!!

        if (y === heapRecord.root && x.isPassive() && !x.isPassiveLinkable()) {
            // if y is the root and x is passive but not linkable
            if (heapRecord.nonLinkableChild === x || heapRecord.nonLinkableChild == null) {
                // the root has only passive and linkable children, so add x to the leftmost
                insertIntoCircularList(lastChild, x, firstChild)
                y.leftChild = x
            } else {
                // add it to the right of the non-linkable child
                insertIntoCircularList(
                    heapRecord.nonLinkableChild!!, x, heapRecord.nonLinkableChild!!.right!!)
            }
        } else {
            insertIntoCircularList(lastChild, x, firstChild)
            if (x.isActive()) y.leftChild = x
        }
    }
}
