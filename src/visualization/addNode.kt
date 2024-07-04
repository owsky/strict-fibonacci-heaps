package visualization

import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord
import org.graphstream.graph.Graph

fun addNode(
    node: NodeRecord<Int>?,
    graph: Graph,
    alreadyAddedNodes: MutableSet<String>,
    alreadyAddedEdges: MutableSet<Pair<String, String>>
) {
    if (node == null) return

    val nodeId = node.item.toString()
    if (nodeId in alreadyAddedNodes) return
    alreadyAddedNodes.add(nodeId)

    val graphNode = graph.addNode(nodeId)

    graphNode.setAttribute("ui.label", node.item.toString())
    graphNode.setAttribute("ui.style", "text-size: 30; text-color: black;")

    val textOffset = if (node.item in 10..99) "text-offset: 0, -8;" else "text-offset: -1, -6;"

    if (node.isPassive()) {
        graphNode.setAttribute(
            "ui.style",
            "fill-color: red; stroke-mode: plain; stroke-color: black; stroke-width: 1px; $textOffset")
    } else {
        graphNode.setAttribute(
            "ui.style",
            "fill-color: white; stroke-mode: plain; stroke-color: black; stroke-width: 1px; $textOffset")
    }

    node.left?.let { left ->
        addNode(left, graph, alreadyAddedNodes, alreadyAddedEdges)
        node.parent?.let { parent ->
            addEdge(parent.item.toString(), left.item.toString(), graph, alreadyAddedEdges)
        }
        //        addEdge(nodeId, it.item.toString(), graph, alreadyAddedEdges)
    }

    node.right?.let { right ->
        addNode(right, graph, alreadyAddedNodes, alreadyAddedEdges)
        node.parent?.let { parent ->
            addEdge(parent.item.toString(), right.item.toString(), graph, alreadyAddedEdges)
        }
        //        addEdge(nodeId, it.item.toString(), graph, alreadyAddedEdges)
    }

    //    node.parent?.let { addEdge(it.item.toString(), nodeId, graph, alreadyAddedEdges) }

    node.leftChild?.let {
        addNode(it, graph, alreadyAddedNodes, alreadyAddedEdges)
        addEdge(nodeId, it.item.toString(), graph, alreadyAddedEdges)
    }
}
