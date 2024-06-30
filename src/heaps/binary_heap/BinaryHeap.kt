package heaps.binary_heap

import heaps.MinHeap
import java.util.*

class BinaryHeap<T : Comparable<T>>(items: Collection<T> = emptyList()) : MinHeap<T>(items) {
    private val lookup: MutableMap<T, Int> = HashMap()
    private val data: MutableList<T> = ArrayList()

    init {
        data.addAll(items)
        for (i in data.indices) {
            lookup[data[i]] = i
        }
        val startIndex = data.size / 2 - 1
        for (i in startIndex downTo 0) {
            heapifyDown(i)
        }
    }

    private fun swap(i: Int, j: Int) {
        Collections.swap(data, i, j)
        lookup[data[i]] = i
        lookup[data[j]] = j
    }

    private fun heapifyUp(i: Int) {
        var index = i
        var parentIndex = getParentIndex(index)
        while (index > 0 && data[index] < data[parentIndex]) {
            swap(index, parentIndex)
            index = parentIndex
            parentIndex = getParentIndex(index)
        }
    }

    private fun heapifyDown(index: Int) {
        var smallestIndex = index
        val leftChildIndex = getLeftChildIndex(index)
        val rightChildIndex = getRightChildIndex(index)

        if (leftChildIndex < data.size && data[leftChildIndex] < data[smallestIndex]) {
            smallestIndex = leftChildIndex
        }

        if (rightChildIndex < data.size && data[rightChildIndex] < data[smallestIndex]) {
            smallestIndex = rightChildIndex
        }

        if (smallestIndex != index) {
            swap(index, smallestIndex)
            heapifyDown(smallestIndex)
        }
    }

    override fun insert(item: T) {
        data.add(item)
        lookup[item] = data.size - 1
        heapifyUp(data.size - 1)
    }

    private fun remove(index: Int) {
        if (data.isEmpty()) {
            throw IndexOutOfBoundsException("Heap is empty")
        }

        if (index >= data.size) {
            throw IndexOutOfBoundsException("Index out of range")
        }

        val lastIndex = data.size - 1
        val removedItem = data[index]

        if (index != lastIndex) {
            swap(index, lastIndex)
            data.removeAt(lastIndex)
            lookup.remove(removedItem)

            if (index > 0 && data[index] < data[getParentIndex(index)]) {
                heapifyUp(index)
            } else {
                heapifyDown(index)
            }
        } else {
            data.removeAt(lastIndex)
            lookup.remove(removedItem)
        }
    }

    override fun extractMin(): T {
        if (data.isEmpty()) {
            throw NoSuchElementException("Trying to extract minimum item from an empty heap")
        }
        val min = data[0]
        remove(0)
        return min
    }

    override fun decreaseKey(key: T, smallerKey: T) {
        val index = lookup[key] ?: throw IllegalArgumentException("Key not found in heap")
        if (smallerKey > data[index]) {
            throw IllegalArgumentException(
                "New key is not smaller than current key. Previous: ${data[index]}, new: $smallerKey")
        }

        data[index] = smallerKey
        lookup.remove(key)
        lookup[smallerKey] = index
        heapifyUp(index)
    }

    override fun getSize(): Int = data.size

    override fun isEmpty(): Boolean = data.isEmpty()

    override fun contains(key: T): Boolean = lookup.containsKey(key)

    companion object {
        private fun getParentIndex(i: Int): Int = (i - 1) / 2

        private fun getLeftChildIndex(i: Int): Int = 2 * i + 1

        private fun getRightChildIndex(i: Int): Int = 2 * i + 2
    }
}
