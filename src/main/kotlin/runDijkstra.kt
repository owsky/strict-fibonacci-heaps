import algorithms.dijkstra
import heaps.HeapKind
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun runDijkstra(kind: HeapKind) {
    logger.info { "Running Dijkstra's algorithm" }

    val (graph, root) = createGraph()

    dijkstra(graph, root, kind)
    graph.printNodesDetails()
}
