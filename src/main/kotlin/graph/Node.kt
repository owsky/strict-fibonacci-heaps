package graph

data class Node(val id: Int) : Comparable<Node> {
    var key: Double = 0.0
    var pred: Node? = null

    override fun equals(other: Any?): Boolean {
        if (other !is Node) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Node: $id with key $key and pred ${pred?.let { "Node ${it.id}" } ?: "nullptr"}"
    }

    override fun compareTo(other: Node): Int {
        val keyComparison = key.compareTo(other.key)
        return if (keyComparison == 0) id.compareTo(other.id) else keyComparison
    }
}
