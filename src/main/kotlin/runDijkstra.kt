import algorithms.dijkstra
import heaps.HeapKind

fun runDijkstra(kind: HeapKind) {
    println("Running Dijkstra's algorithm")

    val (graph, root) = createGraph()

    dijkstra(graph, root, kind)
    graph.printNodesDetails()
}
