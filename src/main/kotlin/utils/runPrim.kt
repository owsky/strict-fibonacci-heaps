package utils

import algorithms.prim
import graph.Graph.Companion.createGraph
import heaps.HeapKind
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun runPrim(kind: HeapKind) {
    logger.info { "Running Prim's algorithm with a $kind" }

    val (graph, root) = createGraph(n = 100, p = 1.0, directed = false, seed = 1234L)
    prim(graph, root, kind)
}
