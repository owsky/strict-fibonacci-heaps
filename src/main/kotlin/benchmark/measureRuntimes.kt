package benchmark

import algorithms.dijkstra
import algorithms.prim
import graph.Graph
import graph.Node
import heaps.HeapKind

private fun measureRuntime(
    n: Int,
    p: Double,
    directed: Boolean,
    seed: Long,
    algorithm: String,
    action: (Graph, Node, HeapKind) -> Unit
): List<BenchmarkResults> {
    val results = mutableListOf<BenchmarkResults>()
    val (graph, root) = Graph.createGraph(n, p, directed, seed)
    for (heapKind in HeapKind.entries) {
        val runtime =
            medianTime(3) {
                val (graphCopy, rootCopy) = graph.copy(root)
                action(graphCopy, rootCopy, heapKind)
            }
        results.add(
            BenchmarkResults(
                algorithm = algorithm,
                heapKind = heapKind.toString(),
                p = p,
                n = n,
                runtime = runtime))
    }
    return results
}

fun measureRuntimes(seed: Long): MutableList<BenchmarkResults> {
    val nodeNumbers = listOf(1000, 5000)
    val completenessDegrees = listOf(0.2, 0.5, 0.9, 1.0)
    val results = mutableListOf<BenchmarkResults>()

    for (n in nodeNumbers) {
        for (p in completenessDegrees) {
            val primRuntimes =
                measureRuntime(n, p, false, seed, "Prim") { graph, root, heapKind ->
                    prim(graph, root, heapKind)
                }
            results.addAll(primRuntimes)

            val dijkstraRuntimes =
                measureRuntime(n, p, true, seed, "Dijkstra") { graph, root, heapKind ->
                    dijkstra(graph, root, heapKind)
                }
            results.addAll(dijkstraRuntimes)
        }
    }

    return results
}
