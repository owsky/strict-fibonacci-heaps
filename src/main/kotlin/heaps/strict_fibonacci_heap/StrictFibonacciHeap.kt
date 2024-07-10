package heaps.strict_fibonacci_heap

import heaps.MinHeap
import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import heaps.strict_fibonacci_heap.transformations.*
import heaps.strict_fibonacci_heap.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/** Strict Fibonacci Heap implementation for Prim's and Dijkstra's algorithms. */
class StrictFibonacciHeap<T : Comparable<T>>(items: Collection<T> = emptyList()) : MinHeap<T>() {
    private var heapRecord = HeapRecord<T>()
    private val lookup: MutableMap<T, NodeRecord<T>> = mutableMapOf()

    init {
        items.forEach { item -> insert(item) }
    }

    /**
     * Inserts [item] into a new Strict Fibonacci Heap and melds it to the current one
     *
     * T(n) = O(1)
     */
    override fun insert(item: T) {
        logger.debug { "Inserting $item" }
        if (heapRecord.root == null) {
            heapRecord.root = NodeRecord(item)
            heapRecord.size = 1
            lookup[item] = heapRecord.root!!
        } else {
            val newHeap = HeapRecord(item)
            meld(newHeap)
        }
    }

    /**
     * Melds [otherHeap] with the current Strict Fibonacci Heap. It is only used when inserting a
     * new item.
     *
     * T(n) = O(1)
     */
    private fun meld(otherHeap: HeapRecord<T>) {
        lookup[otherHeap.root!!.item] = otherHeap.root!!
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
        link(v, u, biggerHeap)

        // merge queues
        val newQueueHead = mergeQueues(smallerHeap, v, biggerHeap)

        // consolidate changes into object's heap record
        if (heapRecord !== biggerHeap) {
            heapRecord = biggerHeap
        }
        heapRecord.size = newSize
        heapRecord.root = u
        heapRecord.qHead = newQueueHead
        if (heapRecord.nonLinkableChild?.parent !== u)
            heapRecord.nonLinkableChild = if (v.isPassiveLinkable()) null else v

        // do one active root reduction and one root degree reduction if possible
        var rootDegreeReductionCounter = 0
        var activeRootReductionCounter = 0
        while ((rootDegreeReductionCounter < 1 && canPerformRootDegreeReduction(heapRecord)) ||
            (activeRootReductionCounter < 1 && canPerformActiveRootReduction(heapRecord))) {
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

    /**
     * Deletes the minimum item from the heap. It is located at the root, so a new root needs to be
     * appointed.
     *
     * T(n) = O(log n)
     */
    private fun deleteMin() {
        --heapRecord.size
        lookup.remove(heapRecord.root!!.item)
        if (heapRecord.root!!.leftChild == null) {
            // if the root is the only element
            heapRecord.root = null
            if (heapRecord.size != 0)
                throw IllegalStateException(
                    "The root is now null but the heap still contains ${heapRecord.size} elements")
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
                heapRecord.qHead = y.qNext
                y.leftChild?.let {
                    val fstLastChild = it.left!!
                    val sndLastChild = fstLastChild.left!!
                    if (fstLastChild.isPassive()) {
                        link(fstLastChild, x, heapRecord)
                    }
                    if (sndLastChild !== fstLastChild && sndLastChild.isPassive()) {
                        link(sndLastChild, x, heapRecord)
                    }
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

    /**
     * Deletes the minimum item from the heap and returns it.
     *
     * T(n) = O(log n)
     */
    override fun extractMin(): T {
        if (getSize() == 0) throw NoSuchElementException("The heap is empty")
        val min =
            heapRecord.root?.item
                ?: throw IllegalStateException("The heap is not empty but the root is null")
        logger.debug { "Deleting $min" }
        deleteMin()
        return min
    }

    /**
     * Given [key] and its replacement [smallerKey], substitutes [smallerKey] in [key]'s place and
     * adjusts the heap so the invariants are still enforced.
     *
     * T(n) = O(1)
     */
    override fun decreaseKey(key: T, smallerKey: T) {
        val x = lookup[key] ?: throw NoSuchElementException("Key not found")
        x.item = smallerKey

        val root = heapRecord.root!!
        if (root === x) {
            return
        } else if (x.item < root.item) {
            root.item = x.item.also { x.item = root.item }
            lookup[root.item] = root
            lookup[x.item] = x
        }

        link(x, root, heapRecord)

        // do a loss reduction if possible
        if (canPerformTwoNodesLossReduction(heapRecord)) performTwoNodesLossReduction(heapRecord)
        else if (canPerformOneNodeLossReduction(heapRecord)) performOneNodeLossReduction(heapRecord)

        // do up to six active root reductions and up to four root degree reductions
        var activeRootReductionsCounter = 0
        var rootDegreeReductionsCounter = 0
        while ((activeRootReductionsCounter < 6 && canPerformActiveRootReduction(heapRecord)) ||
            (rootDegreeReductionsCounter < 4 && canPerformRootDegreeReduction(heapRecord))) {

            if (canPerformActiveRootReduction(heapRecord)) {
                performActiveRootReduction(heapRecord)
                activeRootReductionsCounter++
            }

            if (canPerformRootDegreeReduction(heapRecord)) {
                performRootDegreeReduction(heapRecord)
                rootDegreeReductionsCounter++
            }
        }
    }

    /**
     * Returns the size of the heap.
     *
     * T(n) = O(1)
     */
    override fun getSize(): Int {
        return heapRecord.size
    }

    /**
     * Returns whether the heap is empty.
     *
     * T(n) = O(1)
     */
    override fun isEmpty(): Boolean {
        return heapRecord.size == 0
    }

    /**
     * Returns whether the heap contains [key].
     *
     * T(n) = O(1)
     */
    override fun contains(key: T): Boolean {
        return lookup.containsKey(key)
    }
}
