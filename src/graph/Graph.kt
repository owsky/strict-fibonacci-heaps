package graph

import java.util.AbstractMap.SimpleEntry

class Graph(private var directed: Boolean) {
    var data: MutableMap<Node, MutableSet<Map.Entry<Node, Double>>> = HashMap()

    fun addEdge(u: Node, v: Node, weight: Double) {
        data.computeIfAbsent(u) { HashSet() }.add(SimpleEntry(v, weight))
        if (!directed) {
            data.computeIfAbsent(v) { HashSet() }.add(SimpleEntry(u, weight))
        }
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

    fun getNodes(): Collection<Node> {
        val nodes = java.util.Vector<Node>()
        data.forEach { (node, _) -> nodes.add(node) }
        return nodes
    }
}
