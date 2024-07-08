package utils

import algorithms.dijkstra
import graph.Graph.Companion.createGraph
import heaps.HeapKind
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun runDijkstra(kind: HeapKind) {
    logger.info { "Running Dijkstra's algorithm with a $kind" }

    val (graph, root) = createGraph(n = 20, p = 0.1, directed = true, seed = 1234L)

    dijkstra(graph, root, kind)
    graph.printNodesDetails()
}
