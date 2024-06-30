package algorithms

import graph.Graph
import graph.Node
import heaps.HeapKind
import heaps.MinHeap
import heaps.binary_heap.BinaryHeap
import heaps.strict_fibonacci_heap.StrictFibonacciHeap

fun prim(graph: Graph, s: Node, heapKind: HeapKind) {
    graph.initSingleSource(s)
    val heap: MinHeap<Node> =
        if (heapKind == HeapKind.BINARY_HEAP) BinaryHeap() else StrictFibonacciHeap()

    while (!heap.isEmpty()) {
        val vMin = heap.extractMin()

        for ((v, edgeWeight) in graph.getAdjacencyList(vMin)) {
            if (heap.contains(v) && edgeWeight < v.key) {
                v.key = edgeWeight
                val vSmaller = Node(v.id)
                vSmaller.pred = vMin
                vSmaller.key = edgeWeight
                heap.decreaseKey(v, vSmaller)
            }
        }
    }
}
