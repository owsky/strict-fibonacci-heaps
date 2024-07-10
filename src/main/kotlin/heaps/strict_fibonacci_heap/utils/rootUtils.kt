package heaps.strict_fibonacci_heap.utils

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.transformations.link

/**
 * Returns the child of the root with minimum key.
 *
 * T(n) = O(log n)
 */
fun <T : Comparable<T>> findMinimumRootChild(heapRecord: HeapRecord<T>): NodeRecord<T> {
    if (heapRecord.root == null) throw IllegalArgumentException("The heap is empty")
    else if (heapRecord.root!!.leftChild == null)
        throw IllegalArgumentException("The root has no children")

    var minChild: NodeRecord<T> = heapRecord.root!!.leftChild!!
    heapRecord.root?.leftChild?.forEach { currentChild ->
        if (currentChild.item < minChild.item) minChild = currentChild
        return@forEach true
    }

    return minChild
}

/**
 * Links [x] and all of its siblings to the root.
 *
 * T(n) = O(log n)
 */
fun <T : Comparable<T>> linkAllToRoot(x: NodeRecord<T>?, heapRecord: HeapRecord<T>) {
    val root = heapRecord.root!!
    x?.forEach { node ->
        link(node, root, heapRecord)
        return@forEach true
    }
}

/**
 * Sets [x] as [heapRecord]'s new root node.
 *
 * T(n) = O(log n)
 */
fun <T : Comparable<T>> setNewRoot(x: NodeRecord<T>, heapRecord: HeapRecord<T>) {
    val previousRoot = heapRecord.root!!

    // if x is active, set it to passive and all active children become active roots
    if (x.isActive()) x.setPassive(heapRecord)

    // remove x from the queue
    removeFromQueue(x, heapRecord)

    // set the root pointer to x
    heapRecord.root = x

    // update previous root's leftChild pointer if needed
    if (previousRoot.leftChild === x) {
        previousRoot.leftChild = if (x.right !== x) x.right else null
    }

    // set the new non-linkable child
    heapRecord.nonLinkableChild = null
    x.leftChild?.forEach { currentChild ->
        if (!currentChild.isPassiveLinkable()) {
            if (heapRecord.nonLinkableChild == null) {
                heapRecord.nonLinkableChild = currentChild
            } else if (heapRecord.nonLinkableChild!!.isActive()) {
                heapRecord.nonLinkableChild = currentChild
            }
            return@forEach true
        }
        // early return if current child is passive and linkable
        return@forEach false
    }

    // update x's and its siblings' left and right pointers
    removeFromSiblings(x)

    // set the x's parent to null
    x.parent = null

    // rearrange root children in order to restore invariant I1: structure
    //    rearrangeRootChildren(heapRecord)

    // make all the other children of the root children of x
    linkAllToRoot(previousRoot.leftChild, heapRecord)
}

/**
 * Rearranged the order of the root's children so that invariant I1 is enforced, thus the children
 * are ordered as: active nodes -> passive nodes -> passive and linkable nodes
 *
 * T(n) = O(log n)
 */
fun <T : Comparable<T>> rearrangeRootChildren(heapRecord: HeapRecord<T>) {
    val root = heapRecord.root!!
    val activeChildren: MutableList<NodeRecord<T>> = mutableListOf()
    val passiveChildren: MutableList<NodeRecord<T>> = mutableListOf()
    val passiveLinkableChildren: MutableList<NodeRecord<T>> = mutableListOf()

    root.leftChild?.forEach { child ->
        child.left = null
        child.right = null
        if (child.isActive()) activeChildren.add(child)
        else if (child.isPassiveLinkable()) passiveLinkableChildren.add(child)
        else passiveChildren.add(child)
    }

    root.leftChild = null

    listOf(activeChildren, passiveChildren, passiveLinkableChildren).forEach { nodesList ->
        nodesList.forEach { node ->
            if (root.leftChild == null) {
                root.leftChild = node
                node.left = node
                node.right = node
            } else {
                val firstChild = root.leftChild!!
                val lastChild = firstChild.left!!

                if (firstChild === lastChild) {
                    // I've only inserted one child yet
                    firstChild.right = node
                    firstChild.left = node
                    node.right = firstChild
                    node.left = firstChild
                } else {
                    lastChild.right = node
                    firstChild.left = node
                    node.left = lastChild
                    node.right = firstChild
                }
            }
        }
    }
}

fun <T : Comparable<T>> updateNonLinkableChild(x: NodeRecord<T>, heapRecord: HeapRecord<T>) {
    if (!x.isPassiveLinkable()) {
        if (heapRecord.nonLinkableChild == null) heapRecord.nonLinkableChild = x
        else if (heapRecord.nonLinkableChild!!.isActive() && x.isPassive())
            heapRecord.nonLinkableChild = x
    }
}
