package visualization

import java.awt.event.MouseEvent
import java.util.*
import javax.swing.JLabel
import javax.swing.JPopupMenu
import org.graphstream.graph.Node
import org.graphstream.ui.graphicGraph.GraphicGraph
import org.graphstream.ui.swing_viewer.util.DefaultMouseManager
import org.graphstream.ui.view.View
import org.graphstream.ui.view.util.InteractiveElement

class TreeLayoutMouseManager : DefaultMouseManager() {

    private val popup = JPopupMenu()

    override fun init(graph: GraphicGraph?, view: View?) {
        super.init(graph, view)
    }

    override fun mouseMoved(e: MouseEvent) {
        val point = e.point
        val types = EnumSet.of(InteractiveElement.NODE, InteractiveElement.SPRITE)
        val element = view.findGraphicElementAt(types, point.x.toDouble(), point.y.toDouble())

        if (element != null && element is Node) {
            showPopup(e, element)
        } else {
            popup.isVisible = false
        }
    }

    private fun showPopup(e: MouseEvent, node: Node) {
        popup.removeAll()
        val isActiveRoot = node.getAttribute("ui.activeRoot")
        val loss = node.getAttribute("ui.loss")
        val isNonLinkableChild = node.getAttribute("ui.nonLinkableChild")
        val rank = node.getAttribute("ui.rank")

        var text = ""

        fun addComma(str: String) = if (str.isNotEmpty()) ", " else ""

        rank?.let { text += "Rank $rank" }
        isActiveRoot?.let { text += "${addComma(text)}Active Root" }
        loss?.let { text += "${addComma(text)}Loss $loss" }
        isNonLinkableChild?.let { text += "${addComma(text)}Non-Linkable Child" }
        popup.add(JLabel(text))

        // Display the popup
        popup.pack()

        // Calculate new position for the popup
        val offsetX = 10 // Offset to the right
        val offsetY = -popup.height - 10 // Offset above the cursor

        // Ensure the popup stays within the component's bounds
        val component = e.component
        var x = e.x + offsetX
        var y = e.y + offsetY

        if (x + popup.width > component.width) {
            x = component.width - popup.width
        }
        if (y < 0) {
            y = e.y + 10 // If it doesn't fit above, show it below
        }

        if (text.isNotEmpty()) popup.show(component, x, y)
    }
}
