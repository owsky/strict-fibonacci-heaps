package visualization

import heaps.strict_fibonacci_heap.StrictFibonacciHeap
import java.awt.Dimension
import javax.swing.JFrame
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.ui.swing_viewer.ViewPanel

fun <T : Comparable<T>> visualizeTree(heap: StrictFibonacciHeap<T>) {
    System.setProperty("org.graphstream.ui", "swing")
    val graph = SingleGraph("MyGraph")
    graph.setAttribute("ui.antialias")
    graph.setAttribute("ui.quality")
    graph.setAttribute("ui.stylesheet", "node {size: 60px, 60px;}")

    val viewer = graph.display()
    viewer.disableAutoLayout()

    val viewPanel = viewer.defaultView as ViewPanel
    viewPanel.enableMouseOptions()

    val frame = JFrame("GraphStream Viewer")
    frame.contentPane.add(viewPanel)
    frame.size = Dimension(1920, 1080)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true

    val alreadyAddedNodes = mutableSetOf<String>()
    val alreadyAddedEdges = mutableSetOf<Pair<String, String>>()
    addNode(heap.heapRecord.root, graph, alreadyAddedNodes, alreadyAddedEdges)

    val t = TreeLayout(heap.heapRecord.root!!.item.toString())
    t.init(graph)
    t.compute()
}
