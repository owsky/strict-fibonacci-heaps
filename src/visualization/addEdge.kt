package visualization

import org.graphstream.graph.Graph

fun addEdge(
    from: String,
    to: String,
    graph: Graph,
    alreadyAddedEdges: MutableSet<Pair<String, String>>
) {
    if ((from to to) !in alreadyAddedEdges) {
        graph.addEdge("${from}-${to}", from, to, true)
        alreadyAddedEdges.add(from to to)
    }
}
