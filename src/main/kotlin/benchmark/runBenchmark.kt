package benchmark

import algorithms.dijkstra
import algorithms.prim
import graph.Graph
import heaps.HeapKind
import kotlin.time.measureTime

fun runBenchmark(seed: Long) {
    val completenessDegrees = listOf(0.1, 0.3, 0.5, 0.9, 1.0)
    val nodeNumbers = listOf(100, 1000)

    // benchmark Prim
    for (heapKind in HeapKind.entries) {
        for (p in completenessDegrees) {
            for (n in nodeNumbers) {
                val (graph, root) = Graph.createGraph(n, p, false, seed)
                val runtime = measureTime { prim(graph, root, heapKind) }

                println(
                    "Prim with $heapKind and a graph made of $n nodes with p = $p took $runtime")
            }
        }
    }

    // benchmark Dijkstra
    for (heapKind in HeapKind.entries) {
        for (p in completenessDegrees) {
            for (n in nodeNumbers) {
                val (graph, root) = Graph.createGraph(n, p, true, seed)
                val runtime = measureTime { dijkstra(graph, root, heapKind) }
                println(
                    "Dijkstra with $heapKind and a graph made of $n nodes with p = $p took $runtime")
            }
        }
    }
}
