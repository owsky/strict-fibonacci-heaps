package visualization

import heaps.strict_fibonacci_heap.StrictFibonacciHeap
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
    var currentIndex = 0

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

        fun updateVisualization() {
            graph.clear()
            addNode(heap.heapRecord.root, graph)

            val heapSize = heap.getSize().toDouble()
            val t =
                TreeLayout(heap.heapRecord.root!!.item.toString(), heapSize * 3, heapSize.pow(1.5))
            t.init(graph)
            t.compute()

            // Reapply styles after updating the graph
            applyStyles()
        }

        if (heap.getSize() > 0) updateVisualization()

        val dialog = JDialog()
        dialog.title = "Interactive Tree Visualization"
        dialog.layout = BorderLayout()
        dialog.add(viewPanel, BorderLayout.CENTER)

        val buttonPanel = JPanel()
        val addNodeButton = JButton("Add Node")
        val closeButton = JButton("Close")

        addNodeButton.addActionListener {
            if (currentIndex < nums.size) {
                heap.insert(nums[currentIndex])
                currentIndex++
                updateVisualization()
                addNodeButton.text =
                    if (currentIndex < nums.size) "Add Node (${nums[currentIndex]})"
                    else "No More Nodes"
                addNodeButton.isEnabled = currentIndex < nums.size
            }
        }

        closeButton.addActionListener {
            dialog.dispose()
            latch.countDown()
            exitProcess(0)
        }

        buttonPanel.add(addNodeButton)
        buttonPanel.add(closeButton)
        dialog.add(buttonPanel, BorderLayout.SOUTH)

        dialog.size = Dimension(1920, 1080)
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
        addNodeButton.text = if (nums.isNotEmpty()) "Add Node (${nums[0]})" else "No Nodes to Add"
        addNodeButton.isEnabled = nums.isNotEmpty()

        dialog.isVisible = true
    }

    latch.await()
}
