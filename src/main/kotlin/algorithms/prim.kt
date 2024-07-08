package algorithms

import graph.Graph
import graph.Node
import heaps.HeapKind
import heaps.MinHeap
import heaps.binary_heap.BinaryHeap
import heaps.strict_fibonacci_heap.StrictFibonacciHeap

fun prim(graph: Graph, s: Node, heapKind: HeapKind) {
    graph.initSingleSource(s)
    val nodes = graph.getNodes()
    val heap: MinHeap<Node> =
        when (heapKind) {
            HeapKind.BINARY_HEAP -> {
                BinaryHeap(nodes)
            }
            HeapKind.STRICT_FIBONACCI_HEAP -> {
                StrictFibonacciHeap(nodes)
            }
        }

    while (!heap.isEmpty()) {
        val vMin = heap.extractMin()

        for ((v, edgeWeight) in graph.getAdjacencyList(vMin)) {
            if (heap.contains(v) && edgeWeight < v.key) {
                val vSmaller = Node(v.id)
                vSmaller.pred = vMin
                vSmaller.key = edgeWeight
                heap.decreaseKey(v, vSmaller)
                v.key = edgeWeight
                v.pred = vMin
            }
        }
    }
}
