package visualization

import java.util.*
import org.graphstream.algorithm.Algorithm
import org.graphstream.graph.Graph
import org.graphstream.graph.Node

class TreeLayout(private val rootId: String) : Algorithm {
    private var graph: Graph? = null
    private val xOffset = 100.0
    private val yOffset = 50.0

    override fun init(graph: Graph?) {
        this.graph = graph
    }

    override fun compute() {
        if (graph == null) throw RuntimeException("No graph set for layout")
        val root = graph!!.getNode(rootId) ?: throw RuntimeException("Root node not found")

        // Reset all nodes' positions and depths
        for (node in graph!!) {
            node.setAttribute("xy", doubleArrayOf(0.0, 0.0))
            node.setAttribute("depth", -1)
        }

        // Perform BFS to determine depth of each node
        val queue: Queue<Node> = LinkedList()
        queue.offer(root)
        root.setAttribute("depth", 0)

        while (!queue.isEmpty()) {
            val current = queue.poll()
            val currentDepth = current.getAttribute("depth") as Int

            // Add unvisited neighbors to queue
            for (neighbor in current.neighborNodes()) {
                if (neighbor.getAttribute("depth") == -1) {
                    neighbor.setAttribute("depth", currentDepth + 1)
                    queue.offer(neighbor)
                }
            }
        }

        // Calculate the total width of the tree
        val totalWidth = calculateSubtreeWidth(root)

        // Start the tree layout with the root in the center
        layoutNode(root, 0, totalWidth / 2.0)
    }

    private fun layoutNode(node: Node, depth: Int, xPosition: Double): Double {
        val children = getChildren(node)
        val numChildren = children.size

        if (numChildren == 0) {
            // No children, just place the node
            node.setAttribute("xy", doubleArrayOf(xPosition, -depth * yOffset))
            return xPosition
        }

        // Calculate the required width for each subtree
        val childPositions = mutableListOf<Double>()
        var currentX = xPosition - calculateSubtreeWidth(node) / 2.0

        for (child in children) {
            val childWidth = calculateSubtreeWidth(child)
            val childPosition = currentX + childWidth / 2.0
            childPositions.add(childPosition)
            currentX += childWidth + xOffset
        }

        // Set position of the current node in the middle of its children
        val nodeX =
            if (node === graph!!.getNode(rootId)) 660.0
            else (childPositions.first() + childPositions.last()) / 2.0
        node.setAttribute("xy", doubleArrayOf(nodeX, -depth * yOffset))

        // Recursively layout children
        for ((index, child) in children.withIndex()) {
            layoutNode(child, depth + 1, childPositions[index])
        }

        return nodeX
    }

    private fun getChildren(node: Node): List<Node> {
        val children: MutableList<Node> = ArrayList()
        for (neighbor in node.neighborNodes()) {
            // Only add the neighbor if it is below the current node in the tree
            if (getDepth(neighbor) > getDepth(node)) {
                children.add(neighbor)
            }
        }
        return children
    }

    private fun getDepth(node: Node): Int {
        // Depth attribute should have been set by BFS
        return (node.getAttribute("depth") as? Int) ?: 0
    }

    private fun calculateSubtreeWidth(node: Node): Double {
        val children = getChildren(node)
        if (children.isEmpty()) return xOffset
        var width = 0.0
        for (child in children) {
            width += calculateSubtreeWidth(child)
        }
        return width + (children.size - 1) * xOffset
    }
}
