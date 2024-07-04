package heaps.strict_fibonacci_heap.auxiliary_structures

class FixListRecord<T : Comparable<T>>(val node: NodeRecord<T>, var rank: RankListRecord<T>) {
    var left: FixListRecord<T>? = null
    var right: FixListRecord<T>? = null

    fun forEach(action: (FixListRecord<T>) -> Unit) {
        val visited = mutableSetOf<FixListRecord<T>>()
        var current: FixListRecord<T>? = this

        while (current != null && current !in visited) {
            action(current)
            visited.add(current)
            current = current.right
        }
    }
}
