package heaps.strict_fibonacci_heap

import heaps.MinHeap
import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.transformations.*
import heaps.strict_fibonacci_heap.utils.*

class StrictFibonacciHeap<T : Comparable<T>>(items: Collection<T> = emptyList()) :
    MinHeap<T>(items) {
    var heapRecord = HeapRecord<T>()
    private val lookup: MutableMap<T, NodeRecord<T>> = mutableMapOf()

    init {
        items.forEach { item ->
            //
            insert(item)
        }
    }

    override fun insert(item: T) {
        if (heapRecord.root == null) {
            heapRecord.root = NodeRecord(item)
            heapRecord.size = 1
            lookup[item] = heapRecord.root!!
        } else {
            val newHeap = HeapRecord(item)
            meld(newHeap)
        }
    }

    private fun meld(otherHeap: HeapRecord<T>) {
        lookup[otherHeap.root!!.item] = otherHeap.root!!
        val previousRoot = heapRecord.root!!
        val newSize = heapRecord.size + otherHeap.size

        val smallerHeap: HeapRecord<T>
        val biggerHeap: HeapRecord<T>
        if (heapRecord.size <= otherHeap.size) {
            smallerHeap = heapRecord
            biggerHeap = otherHeap
        } else {
            smallerHeap = otherHeap
            biggerHeap = heapRecord
        }

        val x = smallerHeap.root!!
        val y = biggerHeap.root!!

        smallerHeap.activeRecord.flag = false

        val (u, v) = sortPair(x, y)
        link(v, u, heapRecord)

        // merge queues
        val newQueueHead = mergeQueues(smallerHeap, v, biggerHeap)

        // consolidate changes into object's heap record
        if (heapRecord !== biggerHeap) {
            heapRecord = biggerHeap
        }
        heapRecord.size = newSize
        heapRecord.root = u
        heapRecord.qHead = newQueueHead

        if (previousRoot !== heapRecord.root) heapRecord.nonLinkableChild = null
        if (heapRecord.nonLinkableChild == null && !v.isPassiveLinkable())
            heapRecord.nonLinkableChild = v

        // do one active root reduction and one root degree reduction if possible
        var rootDegreeReductionCounter = 0
        var activeRootReductionCounter = 0
        while ((canPerformActiveRootReduction(heapRecord) ||
            canPerformRootDegreeReduction(heapRecord)) &&
            (rootDegreeReductionCounter < 1 || activeRootReductionCounter < 1)) {
            if (rootDegreeReductionCounter < 1 && canPerformRootDegreeReduction(heapRecord)) {
                performRootDegreeReduction(heapRecord)
                ++rootDegreeReductionCounter
            }
            if (activeRootReductionCounter < 1 && canPerformActiveRootReduction(heapRecord)) {
                performActiveRootReduction(heapRecord)
                ++activeRootReductionCounter
            }
        }
    }

    private fun deleteMin() {
        --heapRecord.size
        lookup.remove(heapRecord.root!!.item)
        if (heapRecord.root!!.leftChild == null) {
            // if the root is the only element
            heapRecord.root = null
            return
        }

        // find the minimum child x of the root
        val x = findMinimumRootChild(heapRecord)

        // set x as the new root
        setNewRoot(x, heapRecord)

        // repeat twice: move the front node y on Q to the back and link the two rightmost children
        // of y to x, if they are passive
        repeat(2) {
            heapRecord.qHead?.let { y ->
                moveToBackOfQueue(y, heapRecord)
                y.leftChild?.let {
                    val fstLastChild = it.left!!
                    val sndLastChild = fstLastChild.left!!
                    if (fstLastChild.isPassive()) link(fstLastChild, x, heapRecord)
                    if (sndLastChild !== fstLastChild && sndLastChild.isPassive())
                        link(sndLastChild, x, heapRecord)
                }
            }
        }

        // do a loss reduction if possible
        if (canPerformTwoNodesLossReduction(heapRecord)) performTwoNodesLossReduction(heapRecord)
        else if (canPerformOneNodeLossReduction(heapRecord)) performOneNodeLossReduction(heapRecord)

        // do active root reductions and root degree reductions in any order until none of either is
        // possible
        while (canPerformActiveRootReduction(heapRecord) ||
            canPerformRootDegreeReduction(heapRecord)) {
            if (canPerformActiveRootReduction(heapRecord)) performActiveRootReduction(heapRecord)
            if (canPerformRootDegreeReduction(heapRecord)) performRootDegreeReduction(heapRecord)
        }
    }

    override fun extractMin(): T {
        if (getSize() == 0) throw NoSuchElementException("The heap is empty")
        val min =
            heapRecord.root?.item
                ?: throw IllegalStateException("The heap is not empty but the root is null")
        deleteMin()
        return min
    }

    override fun decreaseKey(key: T, smallerKey: T) {
        val x = lookup[key] ?: throw NoSuchElementException("Key not found")
        x.item = smallerKey
        val root = heapRecord.root!!
        if (root === x) {
            return
        } else if (smallerKey < root.item) {
            val tmp = root.item
            root.item = smallerKey
            x.item = tmp
            val y = x.parent!!

            val wasXActive = x.isActive()
            val wasXActiveRoot = x.isActiveRoot()

            link(x, root, heapRecord)

            if (wasXActive && !wasXActiveRoot) {
                // x becomes an active root
                x.setLoss(0u, heapRecord)
                moveToActiveRoots(x, heapRecord)
                // decrease rank of y by one
                y.decreaseRank(heapRecord)
            }

            // do a loss reduction if possible
            val lastInFix = heapRecord.fixList!!
            val sndLastInFix = lastInFix.left!!
            if (lastInFix.node.isActive() &&
                !lastInFix.node.isActiveRoot() &&
                lastInFix.node.loss!! >= 2u)
                oneNodeLossReduction(lastInFix.node, heapRecord)
            else if (lastInFix.node.isActive() &&
                !lastInFix.node.isActiveRoot() &&
                sndLastInFix.node.isActive() &&
                !sndLastInFix.node.isActiveRoot() &&
                lastInFix.node.loss!! == 1u &&
                sndLastInFix.node.loss!! == 1u)
                twoNodesLossReduction(lastInFix.node, sndLastInFix.node, heapRecord)

            // do six active root reductions and four root degree reductions in any order
            var activeRootReductionsCounter = 0
            var rootDegreeReductionsCounter = 0
            while (activeRootReductionsCounter < 6 && rootDegreeReductionsCounter < 4) {
                // check if active root reduction is possible
                if (canPerformActiveRootReduction(heapRecord)) {
                    performActiveRootReduction(heapRecord)
                    ++activeRootReductionsCounter
                }

                // check if root degree reduction is possible
                if (canPerformRootDegreeReduction(heapRecord)) {
                    performRootDegreeReduction(heapRecord)
                    ++rootDegreeReductionsCounter
                }
            }
        }
    }

    override fun getSize(): Int {
        return heapRecord.size
    }

    override fun isEmpty(): Boolean {
        return heapRecord.size == 0
    }

    override fun contains(key: T): Boolean {
        return lookup.containsKey(key)
    }
}
