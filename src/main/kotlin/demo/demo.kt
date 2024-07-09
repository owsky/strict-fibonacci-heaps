package demo

import graph.Graph
import graph.Node
import heaps.strict_fibonacci_heap.StrictFibonacciHeap
import visualization.visualizeTreeInteractive

const val visualize = true
const val shortcut = false

fun demo(seed: Long) {
    val heap: StrictFibonacciHeap<Node>
    if (visualize) {
        val (graph, _) = Graph.createGraph(n = 25, p = 0.1, directed = false, seed = seed)
        val nodes = graph.getNodes()
        if (shortcut) {
            heap = StrictFibonacciHeap(nodes)
            visualizeTreeInteractive(heap, nodes)
        } else {
            heap = StrictFibonacciHeap()
            visualizeTreeInteractive(heap, nodes)
        }
    } else {
        val (graph, _) = Graph.createGraph(n = 250, p = 0.1, directed = false, seed = seed)
        val nodes = graph.getNodes()
        heap = StrictFibonacciHeap(nodes)
        val extracted = ArrayList<Node>()
        while (!heap.isEmpty()) extracted.add(heap.extractMin())

        if (nodes.sorted() != extracted) {
            println(nodes.sorted())
            println(extracted)
            throw IllegalStateException("The heap is returning the wrong items")
        } else println("Success")
    }
}
