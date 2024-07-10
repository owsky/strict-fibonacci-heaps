package heaps

/** Abstract class which defines the methods that min heaps should expose. */
abstract class MinHeap<T : Comparable<T>> {
    // Insert an item not currently in the heap
    abstract fun insert(item: T)

    // Deletes the item with minimum key from the heap and returns it
    abstract fun extractMin(): T

    // Decrease the key of a specific item
    abstract fun decreaseKey(key: T, smallerKey: T)

    // Get the size of the heap
    abstract fun getSize(): Int

    // Check if the heap is empty
    abstract fun isEmpty(): Boolean

    // Check if the heap contains a specific key
    abstract fun contains(key: T): Boolean
}
