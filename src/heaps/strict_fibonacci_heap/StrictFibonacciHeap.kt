package heaps.strict_fibonacci_heap

import heaps.MinHeap

class StrictFibonacciHeap<T : Comparable<T>> : MinHeap<T> {
    constructor()

    constructor(vec: List<T>)

    override fun insert(item: T) {}

    override fun extractMin(): T {
        TODO()
    }

    override fun decreaseKey(key: T, smallerKey: T) {}

    override fun getSize(): Int {
        return 0
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun contains(key: T): Boolean {
        return false
    }
}
