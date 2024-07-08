package visualization

import graph.Node
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import kotlin.math.roundToLong
import org.graphstream.graph.Graph

fun addNode(node: NodeRecord<Node>?, graph: Graph, childIndex: Int? = null) {
    if (node == null) return

    val nodeId = node.item.toString()

    val graphNode = graph.addNode(nodeId)
    if (childIndex != null) graphNode.setAttribute("ui.childIndex", childIndex)

    var label =
        if (node.item.key == Double.POSITIVE_INFINITY) "∞" else "${node.item.key.roundToLong()}"
    label += ",${node.item.id}"

    graphNode.setAttribute("ui.label", label)
    graphNode.setAttribute("ui.style", "text-size: 30; text-color: black;")

    val textOffset = if (node.item.id in 10..99) "text-offset: 0, -8;" else "text-offset: -1, -6;"
    val fillColor: String
    if (node.isActiveRoot()) {
        graphNode.setAttribute("ui.activeRoot")
    } else if (node.loss != null && node.loss!! > 0u) {
        graphNode.setAttribute("ui.loss", node.loss!!)
    }
    if (node.isActive()) {
        graphNode.setAttribute("ui.rank", node.getRank().rankNumber)
        fillColor = if (node.isActiveRoot()) "yellow" else "white"
    } else {
        fillColor = "red"
    }
    graphNode.setAttribute(
        "ui.style",
        "fill-color: $fillColor; stroke-mode: plain; stroke-color: black; stroke-width: 1px; $textOffset")

    node.leftChild?.let { firstChild ->
        var currentChildIndex = 0
        var currentChild = firstChild
        addNode(currentChild, graph, currentChildIndex)
        graph.addEdge("${nodeId}-${currentChild.item}", nodeId, currentChild.item.toString(), true)
        currentChild = currentChild.right!!
        ++currentChildIndex
        while (currentChild !== firstChild) {
            addNode(currentChild, graph, currentChildIndex)
            graph.addEdge(
                "${nodeId}-${currentChild.item}", nodeId, currentChild.item.toString(), true)
            currentChild = currentChild.right!!
            ++currentChildIndex
        }
    }
}
