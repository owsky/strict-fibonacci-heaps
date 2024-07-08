package graph

import java.util.AbstractMap.SimpleEntry
import kotlin.math.floor
import kotlin.random.Random
import utils.generateIDs

class Graph {
    private var data: MutableMap<Node, MutableSet<Map.Entry<Node, Double>>> = HashMap()

    fun addEdge(e: Edge) {
        val (u, v, weight) = e
        data.computeIfAbsent(u) { HashSet() }.add(SimpleEntry(v, weight))
    }

    fun getAdjacencyList(u: Node?): Set<Map.Entry<Node, Double>> {
        return data.getOrDefault(u, emptySet())
    }

    fun printNodesDetails() {
        val keys: List<Node> = data.keys.toList()
        keys.sortedBy { it.id }.forEach { println(it) }
        println()
    }

    fun initSingleSource(source: Node) {
        for (node in data.keys) {
            node.key = Double.POSITIVE_INFINITY
            node.pred = null
        }
        source.key = 0.0
    }

    fun getNodes(): List<Node> {
        val nodes = mutableListOf<Node>()
        data.forEach { (node, _) -> nodes.add(node) }
        return nodes
    }

    companion object {
        private fun generateEdges(
            nodes: List<Node>,
            p: Double,
            directed: Boolean,
            seed: Long
        ): List<Edge> {
            val n = nodes.size
            val totalEdges = n.toLong() * (n.toLong() - 1) / 2
            val m = floor(totalEdges.toDouble() * p)
            require(m <= totalEdges)
            require(p in 0.1..1.0) { "p must be within the range 0.1 to 1.0" }

            if (n < 2) return emptyList() // No edges possible if fewer than 2 nodes

            val random = Random(seed)

            // Generate a spanning tree using a simple algorithm (e.g., Prim's or Kruskal's)
            val spanningTreeEdges = mutableListOf<Edge>()
            val connectedNodes = mutableSetOf(nodes.first())
            val remainingNodes = nodes.toMutableSet().apply { remove(nodes.first()) }

            while (remainingNodes.isNotEmpty()) {
                val node1 = connectedNodes.random(random)
                val node2 = remainingNodes.random(random)

                val weight = random.nextDouble(1.0, 100.0)
                spanningTreeEdges.add(Edge(node1, node2, weight))

                connectedNodes.add(node2)
                remainingNodes.remove(node2)
            }

            // Generate all other possible edges
            val allEdges = mutableListOf<Pair<Node, Node>>()
            for (i in 0..<n) {
                for (j in i + 1..<n) {
                    if (directed) {
                        if (!spanningTreeEdges.any {
                            (it.u == nodes[i] && it.v == nodes[j]) ||
                                (it.u == nodes[j] && it.v == nodes[i])
                        }) {
                            allEdges.add(Pair(nodes[i], nodes[j]))
                        }
                    } else {
                        if (!spanningTreeEdges.any { it.u == nodes[i] && it.v == nodes[j] }) {
                            allEdges.add(Pair(nodes[i], nodes[j]))
                        }
                        if (!spanningTreeEdges.any { it.u == nodes[j] && it.v == nodes[i] }) {
                            allEdges.add(Pair(nodes[j], nodes[i]))
                        }
                    }
                }
            }

            val edgesToGenerate = if (directed) m else m / 2
            val additionalEdgesToGenerate = edgesToGenerate - spanningTreeEdges.size

            // Shuffle and take the required number of additional edges
            if (additionalEdgesToGenerate > 0) {
                allEdges.shuffle(random)
                val additionalEdges =
                    allEdges.take(additionalEdgesToGenerate.toInt()).map { (node1, node2) ->
                        Edge(node1, node2, random.nextDouble(1.0, 100.0))
                    }
                return spanningTreeEdges + additionalEdges
            }
            return spanningTreeEdges
        }

        fun createGraph(n: Int, p: Double, directed: Boolean, seed: Long): Pair<Graph, Node> {
            val nums = generateIDs(count = n, range = 0..Int.MAX_VALUE, seed = seed)
            val nodes = nums.map { Node(it) }
            val edges = generateEdges(nodes, p, directed, seed)
            val graph = Graph()
            edges.forEach { graph.addEdge(it) }

            return graph to nodes[0]
        }
    }
}
