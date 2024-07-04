package heaps.strict_fibonacci_heap.utils

import heaps.strict_fibonacci_heap.auxiliary_structures.FixListRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun <T : Comparable<T>> formatFixNode(
    currentFix: FixListRecord<T>,
    heapRecord: HeapRecord<T>
): String {
    val lastInFix = heapRecord.fixList
    val singles = heapRecord.singles
    var text =
        if (currentFix.node.isActiveRoot())
            "(${currentFix.node.item}, Active Root, Loss: ${currentFix.node.loss}, Rank: ${currentFix.rank.rankNumber}"
        else
            "${currentFix.node.item}, Positive Loss, Loss: ${currentFix.node.loss}, Rank: ${currentFix.rank.rankNumber}"
    if (singles === currentFix) text += ", Singles"
    if (lastInFix === currentFix) text += ", Tail"
    text += ")--"
    return text
}

fun <T : Comparable<T>> printFixList(heapRecord: HeapRecord<T>) {
    val lastInFix = heapRecord.fixList

    lastInFix?.let {
        var currentFix = lastInFix.right!!
        var text = "Current FixList: "
        while (currentFix !== lastInFix) {
            text += formatFixNode(currentFix, heapRecord)
            currentFix = currentFix.right!!
        }
        text += formatFixNode(currentFix, heapRecord)
        logger.debug { text }
    }
}

fun <T : Comparable<T>> checkFixList(heapRecord: HeapRecord<T>) {
    val lastInFix = heapRecord.fixList
    val singles = heapRecord.singles
    val nodes = HashSet<T>()

    // check all nodes active
    lastInFix?.let {
        val firstInFix = lastInFix.right!!
        firstInFix.forEach { currentFix ->
            if (currentFix.node.item in nodes) {
                printFixList(heapRecord)
                throw IllegalStateException(
                    "Node with key: ${currentFix.node.item} is duplicate in the fix-list")
            }
            if (currentFix.node.isPassive())
                throw IllegalStateException(
                    "Node with key: ${currentFix.node.item} is in fix-list and is passive.")
            if (currentFix.node.item !in nodes) nodes.add(currentFix.node.item)
        }
    }

    // if singles, check if all nodes to the right are active with positive loss and all nodes to
    // the left are active roots with zero loss
    singles?.let {
        if (singles.node.isActiveRoot())
            throw IllegalStateException(
                "Singles is pointing to node with key: ${singles.node.item} which is an active root.")
        else if (singles.node.isPassive())
            throw IllegalStateException(
                "Singles is pointing to node with key: ${singles.node.item} which is passive.")
        else if (singles.node.loss == null)
            throw IllegalStateException(
                "Singles is pointing to node with key: ${singles.node.item} which has loss set to null.")
        else if (singles.node.loss == 0u)
            throw IllegalStateException(
                "Singles is pointing to node with key: ${singles.node.item} which has loss set to 0.")

        var currentFix = singles
        while (currentFix !== lastInFix!!.right!!) {
            if (currentFix!!.node.loss == null)
                throw IllegalStateException(
                    "Node with key: ${currentFix.node.item} is in parts 3-4 with loss set to null")
            else if (currentFix.node.loss == 0u)
                throw IllegalStateException(
                    "Node with key: ${currentFix.node.item} is in parts 3-4 with loss set to 0")
            currentFix = currentFix.right!!
        }
        currentFix = singles.left
        while (currentFix !== lastInFix) {
            if (!currentFix!!.node.isActiveRoot())
                throw IllegalStateException(
                    "Node with key: ${currentFix.node.item} is in parts 1-2 and is not an active root")
            else if (currentFix.node.loss == null)
                throw IllegalStateException(
                    "Node with key: ${currentFix.node.item} is in parts 1-2 with loss set to null")
            else if (currentFix.node.loss != 0u)
                throw IllegalStateException(
                    "Node with key: ${currentFix.node.item} is in parts 1-2 with loss higher than 0")
            currentFix = currentFix.left!!
        }
    }

    printFixList(heapRecord)
}
