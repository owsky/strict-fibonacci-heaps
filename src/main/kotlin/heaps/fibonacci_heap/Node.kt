package heaps.fibonacci_heap

class Node<T : Comparable<T>>(var key: T) {
    var parent: Node<T>? = null
    var left: Node<T>? = this
    var right: Node<T>? = this
    var child: Node<T>? = null
    var degree: Int = 0
    var mark: Boolean = false
}
