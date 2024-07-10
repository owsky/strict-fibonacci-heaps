package heaps.strict_fibonacci_heap.auxiliary_structures

/**
 * Enum which defines the four different parts of the fix-list. Part one holds the active roots of
 * active-roots-transformable ranks. Part two holds the active roots of non-transformable ranks.
 * Part three holds the active nodes with positive loss of non-transformable ranks. Part four holds
 * the active nodes with positive loss of loss-transformable ranks.
 */
enum class FixListPart {
    PART_ONE,
    PART_TWO,
    PART_THREE,
    PART_FOUR,
}
