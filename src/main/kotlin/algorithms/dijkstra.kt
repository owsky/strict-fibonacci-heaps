package algorithms

import graph.Graph
import graph.Node
import heaps.HeapKind
import heaps.MinHeap
import heaps.binary_heap.BinaryHeap
import heaps.fibonacci_heap.FibonacciHeap
import heaps.strict_fibonacci_heap.StrictFibonacciHeap

fun dijkstra(graph: Graph, s: Node, heapKind: HeapKind) {
    val x: MutableSet<Node> = HashSet()
    graph.initSingleSource(s)
    val nodes = graph.getNodes()
    val heap: MinHeap<Node> =
        when (heapKind) {
            HeapKind.BINARY_HEAP -> {
                BinaryHeap(nodes)
            }
            HeapKind.FIBONACCI_HEAP -> {
                FibonacciHeap(nodes)
            }
            HeapKind.STRICT_FIBONACCI_HEAP -> {
                StrictFibonacciHeap(nodes)
            }
        }

    while (!heap.isEmpty()) {
        val wMin = heap.extractMin()
        x.add(wMin)

        for ((y, edgeWeight) in graph.getAdjacencyList(wMin)) {
            if (!x.contains(y)) {
                val relaxed = wMin.key + edgeWeight
                if (y.key > relaxed) {
                    val ySmaller = Node(y.id)
                    ySmaller.key = relaxed
                    heap.decreaseKey(y, ySmaller)
                    y.key = relaxed
                }
            }
        }
    }
}
