package graph

import java.util.AbstractMap.SimpleEntry
import kotlin.math.floor
import kotlin.random.Random

class Graph {
    private var data: MutableMap<Node, MutableSet<Map.Entry<Node, Double>>> = HashMap()

    fun addEdge(e: Edge) {
        val (u, v, weight) = e
        data.computeIfAbsent(u) { HashSet() }.add(SimpleEntry(v, weight))
        data.computeIfAbsent(v) { HashSet() }
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
        private fun generateWeight(random: Random) = floor(random.nextDouble(1.0, 100.0))

        private fun generateSpanningTree(
            nodes: List<Node>,
            random: Random,
            directed: Boolean
        ): MutableList<Edge> {
            val spanningTreeEdges = mutableListOf<Edge>()
            val connectedNodes = mutableSetOf(nodes.first())
            val remainingNodes = nodes.toMutableSet().apply { remove(nodes.first()) }

            while (remainingNodes.isNotEmpty()) {
                val node1 = connectedNodes.random(random)
                val node2 = remainingNodes.random(random)

                val weight = generateWeight(random)
                spanningTreeEdges.add(Edge(node1, node2, weight))
                if (directed) spanningTreeEdges.add(Edge(node2, node1, weight))

                connectedNodes.add(node2)
                remainingNodes.remove(node2)
            }

            return spanningTreeEdges
        }

        private fun generateEdges(
            nodes: List<Node>,
            p: Double,
            directed: Boolean,
            seed: Long
        ): List<Edge> {
            val n = nodes.size.toLong()
            val mCompleteGraph = n * (n - 1) / 2
            val m = floor(mCompleteGraph.toDouble() * p)
            require(m >= n - 1) { "m needs to be >= n - 1 to ensure connectivity" }
            require(p in 0.1..1.0) { "p must be within the range 0.1 to 1.0" }

            if (n < 2) return emptyList()

            val random = Random(seed)

            // generate a random spanning tree with the given nodes
            val spanningTreeEdges = generateSpanningTree(nodes, random, directed)

            // add random edges until m is reached
            while (spanningTreeEdges.size < m) {
                val u = nodes.random(random)
                val v = nodes.random(random)

                spanningTreeEdges.add(Edge(u, v, generateWeight(random)))
            }

            return spanningTreeEdges
        }

        fun createGraph(n: Int, p: Double, directed: Boolean, seed: Long): Pair<Graph, Node> {
            val nums = (1..n).toList()
            val nodes = nums.map { Node(it) }
            val edges = generateEdges(nodes, p, directed, seed)
            val graph = Graph()
            edges.forEach { graph.addEdge(it) }

            return graph to nodes[0]
        }
    }
}
