package heaps.strict_fibonacci_heap.auxiliary_structures

/**
 * Data class which stores the active flag for a Strict Fibonacci Heap. This allows to set the whole
 * tree to passive in O(1) time.
 */
data class ActiveRecord(var flag: Boolean = true, var refCount: Int = 0)
