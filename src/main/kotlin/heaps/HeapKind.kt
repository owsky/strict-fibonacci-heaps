package heaps

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
