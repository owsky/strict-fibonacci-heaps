package heaps.strict_fibonacci_heap.utils

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.transformations.link

fun <T : Comparable<T>> findMinimumRootChild(heapRecord: HeapRecord<T>): NodeRecord<T> {
    val firstChild = heapRecord.root!!.leftChild!!
    var minChild: NodeRecord<T> = firstChild
    var currChild: NodeRecord<T> = firstChild.right!!
    while (currChild !== firstChild) {
        if (currChild.item < minChild.item) minChild = currChild
        currChild = currChild.right!!
    }
    return minChild
}

fun <T : Comparable<T>> updateNonLinkableChild(heapRecord: HeapRecord<T>) {
    val root = heapRecord.root!!
    heapRecord.nonLinkableChild = null
    root.leftChild?.let {
        if (it.isPassive() && it.isPassiveLinkable()) {
            // if the first child is passive and non-linkable, set the pointer
            heapRecord.nonLinkableChild = it
        } else if (it.isActive()) {
            // if it is active then set the pointer but look also for a non-linkable passive sibling
            heapRecord.nonLinkableChild = it
            var currentChild = it.right!!
            while (currentChild !== it) {
                if (currentChild.isPassive() && !currentChild.isPassiveLinkable()) {
                    heapRecord.nonLinkableChild = currentChild
                    break
                }
                currentChild = currentChild.right!!
            }
        }
    }
}

fun <T : Comparable<T>> linkAllToRoot(firstChild: NodeRecord<T>?, heapRecord: HeapRecord<T>) {
    val root = heapRecord.root!!
    firstChild?.let {
        var currentChild = it.right!!
        link(it, root, heapRecord)
        while (currentChild !== it) {
            val nextChild = currentChild.right!!
            link(currentChild, root, heapRecord)
            currentChild = nextChild
        }
    }
}

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

    // update x's and its siblings' left and right pointers
    removeFromCircularList(x)

    // set the x's parent to null
    x.parent = null

    // rearrange root children in order to restore invariant I1: structure
    rearrangeRootChildren(heapRecord)

    // update non-linkable child pointer
    updateNonLinkableChild(heapRecord)

    // make all the other children of the root children of x
    linkAllToRoot(previousRoot.leftChild, heapRecord)
    checkFixList(heapRecord)
}

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
    activeChildren.forEach { currentChild ->
        if (root.leftChild == null) {
            root.leftChild = currentChild
            currentChild.left = currentChild
            currentChild.right = currentChild
        } else {
            val firstChild = root.leftChild!!
            val lastChild = firstChild.left!!

            if (firstChild === lastChild) {
                // I've only inserted one child yet
                firstChild.right = currentChild
                firstChild.left = currentChild
                currentChild.right = firstChild
                currentChild.left = firstChild
            } else {
                lastChild.right = currentChild
                firstChild.left = currentChild
                currentChild.left = lastChild
                currentChild.right = firstChild
            }
        }
    }

    passiveChildren.forEach { currentChild ->
        if (root.leftChild == null) {
            root.leftChild = currentChild
            currentChild.left = currentChild
            currentChild.right = currentChild
        } else {
            val firstChild = root.leftChild!!
            val lastChild = firstChild.left!!

            if (firstChild === lastChild) {
                // I've only inserted one child yet
                firstChild.right = currentChild
                firstChild.left = currentChild
                currentChild.right = firstChild
                currentChild.left = firstChild
            } else {
                lastChild.right = currentChild
                firstChild.left = currentChild
                currentChild.left = lastChild
                currentChild.right = firstChild
            }
        }
    }

    passiveLinkableChildren.forEach { currentChild ->
        if (root.leftChild == null) {
            root.leftChild = currentChild
            currentChild.left = currentChild
            currentChild.right = currentChild
        } else {
            val firstChild = root.leftChild!!
            val lastChild = firstChild.left!!

            if (firstChild === lastChild) {
                // I've only inserted one child yet
                firstChild.right = currentChild
                firstChild.left = currentChild
                currentChild.right = firstChild
                currentChild.left = firstChild
            } else {
                lastChild.right = currentChild
                firstChild.left = currentChild
                currentChild.left = lastChild
                currentChild.right = firstChild
            }
        }
    }
}
