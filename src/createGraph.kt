import graph.Graph
import graph.Node

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
