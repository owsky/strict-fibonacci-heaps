package heaps.strict_fibonacci_heap

import heaps.MinHeap
import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.transformations.*
import heaps.strict_fibonacci_heap.utils.moveToActiveRoots

class StrictFibonacciHeap<T : Comparable<T>>(items: Collection<T> = emptyList()) :
    MinHeap<T>(items) {
    private val heapRecord = HeapRecord<T>()
    private val lookup: MutableMap<T, NodeRecord<T>> = mutableMapOf()

    init {
        items.forEach { item -> insert(item) }
    }

    override fun insert(item: T) {
        val newHeap = HeapRecord(item)
        meld(newHeap)
    }

    private fun meld(otherHeap: HeapRecord<T>) {
        // insert all other heap's items in lookup
    }

    private fun deleteMin() {}

    override fun extractMin(): T {
        val min = heapRecord.root?.item ?: throw NoSuchElementException("The heap is empty")
        lookup.remove(min)
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

            link(x, root)

            if (wasXActive && !wasXActiveRoot) {
                // x becomes an active root
                x.loss = 0u
                moveToActiveRoots(x, heapRecord)
                // decrease rank of y by one
                y.decreaseRank()
            }

            // do a loss reduction if possible
            val lastInFix = heapRecord.fixList!!
            val sndLastInFix = lastInFix.left
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
                    val firstInFix = heapRecord.fixList!!.right
                    val sndInFix = firstInFix.right
                    activeRootReduction(firstInFix.node, sndInFix.node, heapRecord)
                    ++activeRootReductionsCounter
                }

                // check if root degree reduction is possible
                if (canPerformRootDegreeReduction(heapRecord)) {
                    val fstLastChild = heapRecord.root!!.left!!
                    val sndLastChild = fstLastChild.left!!
                    val trdLastChild = sndLastChild.left!!
                    rootDegreeReduction(fstLastChild, sndLastChild, trdLastChild, heapRecord)
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
