package visualization

import heaps.strict_fibonacci_heap.StrictFibonacciHeap
import heaps.strict_fibonacci_heap.utils.printDebug
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.concurrent.CountDownLatch
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.math.pow
import kotlin.system.exitProcess
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.ui.swing_viewer.SwingViewer
import org.graphstream.ui.swing_viewer.ViewPanel
import org.graphstream.ui.view.Viewer

fun visualizeTreeInteractive(heap: StrictFibonacciHeap<Int>, nums: List<Int>) {
    val latch = CountDownLatch(1)

    SwingUtilities.invokeLater {
        System.setProperty("org.graphstream.ui", "swing")
        val graph = SingleGraph("MyGraph")

        val cssStyles =
            """
                graph {
                    padding: 40px;
                }
                node {
                    size: 60px, 60px;
                }
                edge {
                    shape: line;
                    arrow-size: 10px, 5px;
               }
            """
                .trimIndent()

        fun applyStyles() {
            graph.setAttribute("ui.antialias")
            graph.setAttribute("ui.quality")
            graph.setAttribute("ui.stylesheet", cssStyles)
        }

        applyStyles()

        val viewer = SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD)
        viewer.disableAutoLayout()

        val viewPanel = viewer.addDefaultView(false) as ViewPanel
        viewPanel.setMouseManager(CustomMouseManager())

        fun updateLayout() {
            graph.clear()
            val heapSize = heap.getSize().toDouble()
            if (heapSize > 0.0) {
                addNode(heap.heapRecord.root, graph)

                val t =
                    TreeLayout(
                        heap.heapRecord.root!!.item.toString(), heapSize * 3, heapSize.pow(1.5))
                t.init(graph)
                t.compute()

                // Reapply styles after updating the graph
                applyStyles()
            }
        }

        if (heap.getSize() > 0) updateLayout()

        val dialog = JDialog()
        dialog.title = "Interactive Tree Visualization"
        dialog.layout = BorderLayout()
        dialog.add(viewPanel, BorderLayout.CENTER)

        val buttonPanel = JPanel()
        val addNodeButton = JButton("Add Node")
        val deleteMinButton = JButton("Delete Min")
        val closeButton = JButton("Close")
        val printFixListButton = JButton("Print Fix-List")

        addNodeButton.addActionListener {
            if (heap.getSize() < nums.size) {
                heap.insert(nums[heap.getSize()])
                updateLayout()
                addNodeButton.text =
                    if (heap.getSize() < nums.size) "Add Node (${nums[heap.getSize()]})"
                    else "No More Nodes"
                addNodeButton.isEnabled = heap.getSize() < nums.size
            }
        }

        deleteMinButton.addActionListener {
            if (heap.getSize() > 0) {
                heap.extractMin()
                updateLayout()
            }
        }

        closeButton.addActionListener {
            dialog.dispose()
            latch.countDown()
            exitProcess(0)
        }

        printFixListButton.addActionListener { printDebug(heap.heapRecord) }

        buttonPanel.add(addNodeButton)
        buttonPanel.add(deleteMinButton)
        buttonPanel.add(printFixListButton)
        buttonPanel.add(closeButton)
        dialog.add(buttonPanel, BorderLayout.SOUTH)

        dialog.size = Dimension(1280, 720)
        dialog.setLocationRelativeTo(null) // Center on screen
        dialog.isModal = true

        dialog.addWindowListener(
            object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    dialog.dispose()
                    latch.countDown()
                    exitProcess(0)
                }
            })

        // Set initial text for add node button
        addNodeButton.text =
            if (heap.getSize() < nums.size) "Add Node (${nums[heap.getSize()]})"
            else "No Nodes to Add"
        addNodeButton.isEnabled = nums.isNotEmpty()

        dialog.isVisible = true
    }

    latch.await()
}
