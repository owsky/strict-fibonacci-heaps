package heaps.strict_fibonacci_heap.utils

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord

fun <T : Comparable<T>> checkFixList(heapRecord: HeapRecord<T>) {
    val firstInFix = heapRecord.fixList
    val singles = heapRecord.singles

    // check all nodes active
    firstInFix?.let {
        var currentFix = firstInFix.right!!
        while (currentFix !== firstInFix) {
            if (currentFix.node.isPassive())
                throw IllegalStateException(
                    "Node with key: ${currentFix.node.item} is in fix-list and is passive.")
            currentFix = currentFix.right!!
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
        while (currentFix !== firstInFix) {
            if (currentFix!!.node.loss == null)
                throw IllegalStateException(
                    "Node with key: ${currentFix!!.node.item} is in parts 3-4 with loss set to null")
            else if (currentFix!!.node.loss == 0u)
                throw IllegalStateException(
                    "Node with key: ${currentFix!!.node.item} is in parts 3-4 with loss set to 0")
            currentFix = currentFix!!.right!!
        }
        currentFix = singles.left
        while (currentFix !== firstInFix) {
            if (!currentFix!!.node.isActiveRoot())
                throw IllegalStateException(
                    "Node with key: ${currentFix!!.node.item} is in parts 1-2 and is not an active root")
            else if (currentFix!!.node.loss == null)
                throw IllegalStateException(
                    "Node with key: ${currentFix!!.node.item} is in parts 1-2 with loss set to null")
            else if (currentFix!!.node.loss != 0u)
                throw IllegalStateException(
                    "Node with key: ${currentFix!!.node.item} is in parts 1-2 with loss higher than 0")
            currentFix = currentFix!!.left!!
        }
    }
}
