import algorithms.prim
import heaps.HeapKind

fun runPrim(kind: HeapKind) {
    println("Running Prim's algorithm")

    val (graph, root) = createGraph()

    prim(graph, root, kind)
    graph.printNodesDetails()
}
