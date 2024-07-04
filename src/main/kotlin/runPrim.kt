import algorithms.prim
import heaps.HeapKind
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun runPrim(kind: HeapKind) {
    logger.info { "Running Prim's algorithm" }

    val (graph, root) = createGraph()

    prim(graph, root, kind)
    graph.printNodesDetails()
}
