import algorithms.dijkstra
import algorithms.prim
import graph.Graph
import graph.Node
import heaps.HeapKind
import heaps.strict_fibonacci_heap.StrictFibonacciHeap

fun createGraph(): Pair<Graph, Node> {
    val graph = Graph(true)
    val nodes: MutableList<Node> = ArrayList()

    for (i in 0..4) {
        nodes.add(Node(i))
    }

    graph.addEdge(nodes[0], nodes[1], 5.0)
    graph.addEdge(nodes[0], nodes[3], 2.0)
    graph.addEdge(nodes[1], nodes[2], 8.0)
    graph.addEdge(nodes[1], nodes[4], 9.0)
    graph.addEdge(nodes[3], nodes[1], 4.0)
    graph.addEdge(nodes[2], nodes[4], 6.0)

    return graph to nodes[0]
}

fun runPrim() {
    println("Running Prim's algorithm")

    val (graph, root) = createGraph()

    prim(graph, root, HeapKind.BINARY_HEAP)
    graph.printNodesDetails()
}

fun runDijkstra() {
    println("Running Dijkstra's algorithm")

    val (graph, root) = createGraph()

    dijkstra(graph, root, HeapKind.BINARY_HEAP)
    graph.printNodesDetails()
}

fun main() {
    //    runDijkstra()
    //    runPrim()

    val h: StrictFibonacciHeap<Int> = StrictFibonacciHeap()
    h.insert(5)
    h.insert(3)
    h.insert(10)
    h.insert(8)
    h.insert(9)
    h.insert(4)
    h.insert(1)
    h.decreaseKey(1, 0)
}
