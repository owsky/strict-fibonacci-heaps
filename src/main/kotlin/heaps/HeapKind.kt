package heaps

/**
 * Enum class which discriminates what kind of heap to be used for executing Prim's and Dijkstra's
 * algorithms efficiently.
 */
enum class HeapKind {
    BINARY_HEAP,
    STRICT_FIBONACCI_HEAP;

    override fun toString(): String {
        return when (this) {
            BINARY_HEAP -> "Binary Heap"
            STRICT_FIBONACCI_HEAP -> "Strict Fibonacci Heap"
        }
    }
}
