package visualization

import graph.Node
import heaps.strict_fibonacci_heap.StrictFibonacciHeap
import java.awt.Dimension
import javax.swing.JFrame
import kotlin.math.pow
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.ui.swing_viewer.SwingViewer
import org.graphstream.ui.swing_viewer.ViewPanel
import org.graphstream.ui.view.Viewer

fun visualizeTree(heap: StrictFibonacciHeap<Node>) {
    System.setProperty("org.graphstream.ui", "swing")
    val graph = SingleGraph("MyGraph")
    graph.setAttribute("ui.antialias")
    graph.setAttribute("ui.quality")
    graph.setAttribute("ui.stylesheet", "node {size: 60px, 60px;}")

    val viewer = SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD)
    viewer.disableAutoLayout()

    val viewPanel = viewer.addDefaultView(false) as ViewPanel
    viewPanel.setMouseManager(TreeLayoutMouseManager())

    addNode(heap.heapRecord.root, graph, null, heap.heapRecord)

    val heapSize = heap.getSize().toDouble()
    val t = TreeLayout(heap.heapRecord.root!!.item.toString(), heapSize * 3, heapSize.pow(1.5))
    t.init(graph)
    t.compute()

    val frame = JFrame("GraphStream Viewer")
    frame.contentPane.add(viewPanel)
    frame.size = Dimension(1920, 1080)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true
}
