package heaps.strict_fibonacci_heap.utils

import heaps.strict_fibonacci_heap.auxiliary_structures.FixListRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.RankListRecord
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun <T : Comparable<T>> printDebug(heapRecord: HeapRecord<T>) {
    printFixList(heapRecord)
    printRankList(heapRecord)
}

fun <T : Comparable<T>> printRankList(heapRecord: HeapRecord<T>) {
    var text = "Rank list:"
    var currRank: RankListRecord<T>? = heapRecord.rankList
    while (currRank != null) {
        if (currRank.rankNumber > 0) text += "-->"
        text +=
            "(Rank: ${currRank.rankNumber}, activeRoots: ${currRank.activeRoots?.node?.item}, loss: ${currRank.loss?.node?.item})"
        currRank = currRank.inc
    }

    logger.debug { text }
}

fun <T : Comparable<T>> throwIllegalState(msg: String, heapRecord: HeapRecord<T>) {
    logger.debug { "Illegal state detected. Current status:" }
    printFixList(heapRecord)
    printRankList(heapRecord)
    throw IllegalStateException(msg)
}

fun <T : Comparable<T>> checkRankPointers(rank: RankListRecord<T>, heapRecord: HeapRecord<T>) {
    rank.activeRoots?.let {
        if (!it.node.isActiveRoot())
            throwIllegalState(
                "Rank ${rank.rankNumber}'s activeRoots pointer is pointing to node ${it.node.item} which is not an active root",
                heapRecord)
    }
    rank.loss?.let {
        if (it.node.isActiveRoot()) {
            throwIllegalState(
                "Rank ${rank.rankNumber}'s loss pointer is pointing to node ${it.node.item} which is an active root",
                heapRecord)
        } else if (it.node.loss == 0u) {
            throwIllegalState(
                "Rank ${rank.rankNumber}'s loss pointer is pointing to node ${it.node.item} which does not have positive loss",
                heapRecord)
        }
    }
}

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
            "(${currentFix.node.item}, Positive Loss, Loss: ${currentFix.node.loss}, Rank: ${currentFix.rank.rankNumber}"
    if (singles === currentFix) text += ", Singles"
    if (lastInFix === currentFix) text += ", Tail"
    text += ")"
    return text
}

fun <T : Comparable<T>> printFixList(heapRecord: HeapRecord<T>) {
    val lastInFix = heapRecord.fixList

    lastInFix?.let {
        var currentFix = lastInFix.right!!
        var text = "FixList: "
        while (currentFix !== lastInFix) {
            if (currentFix !== lastInFix.right) text += "--"
            text += formatFixNode(currentFix, heapRecord)
            currentFix = currentFix.right!!
        }
        text += "--" + formatFixNode(currentFix, heapRecord)
        logger.debug { text }
    }
}

fun <T : Comparable<T>> checkFixList(heapRecord: HeapRecord<T>) {
    val lastInFix = heapRecord.fixList
    val singles = heapRecord.singles
    val nodes = HashSet<T>()

    // check all nodes active
    lastInFix?.let {
        var previous: FixListRecord<T>? = null
        val firstInFix = lastInFix.right!!
        firstInFix.forEach { currentFix ->
            if (currentFix.node.item in nodes) {
                throwIllegalState(
                    "Node with key: ${currentFix.node.item} is duplicate in the fix-list",
                    heapRecord)
            } else nodes.add(currentFix.node.item)

            if (currentFix.node.isPassive()) {
                throwIllegalState(
                    "Node with key: ${currentFix.node.item} is in fix-list and is passive.",
                    heapRecord)
            }
            if (previous == null) previous = currentFix
            else {
                if (previous!!.node.isActiveRoot() &&
                    currentFix.node.isActiveRoot() &&
                    !previous!!.rank.isActiveRootTransformable() &&
                    currentFix.rank.isActiveRootTransformable())
                    throwIllegalState(
                        "Previous node ${previous!!.node.item} on the fix-list isn't active root transformable but current ${currentFix.node.item} is",
                        heapRecord)
                else if (!previous!!.node.isActiveRoot() && currentFix.node.isActiveRoot())
                    throwIllegalState(
                        "Previous node ${previous!!.node.item} on the fix-list is not an active root but current ${currentFix.node.item} is",
                        heapRecord)
                else if (!previous!!.node.isActiveRoot() &&
                    !currentFix.node.isActiveRoot() &&
                    previous!!.rank.isLossTransformable() &&
                    !currentFix.rank.isLossTransformable())
                    throwIllegalState(
                        "Previous node ${previous!!.node.item} on the fix-list is loss transformable but current ${currentFix.node.item} isn't",
                        heapRecord)

                if (currentFix.node.isActiveRoot() && currentFix.node.loss!! > 0u)
                    throwIllegalState(
                        "Node ${currentFix.node.item} on the fix-list is an active root but its loss is greater than 0",
                        heapRecord)
                else if (!currentFix.node.isActiveRoot() && currentFix.node.loss!! == 0u)
                    throwIllegalState(
                        "Node ${currentFix.node.item} on the fix-list isn't an active root and its loss is equal to 0",
                        heapRecord)
            }

            if (currentFix.node.isActiveRoot() && currentFix.rank.activeRoots == null)
                throwIllegalState(
                    "Node ${currentFix.node.item} is an active root, but its rank ${currentFix.rank.rankNumber} has its activeRoots pointer set to null",
                    heapRecord)
            else if (!currentFix.node.isActiveRoot() && currentFix.rank.loss == null)
                throwIllegalState(
                    "Node ${currentFix.node.item} is a node with positive loss, but its rank ${currentFix.rank.rankNumber} has its loss pointer set to null",
                    heapRecord)

            if (currentFix.node.loss == null)
                throwIllegalState(
                    "Node ${currentFix.node.item} is on the fix-list with loss set to null",
                    heapRecord)
        }
    }

    // if singles, check if all nodes to the right are active with positive loss and all nodes to
    // the left are active roots with zero loss
    singles?.let {
        if (singles.node.isActiveRoot())
            throwIllegalState(
                "singlesLoss is pointing to node with key: ${singles.node.item} which is an active root.",
                heapRecord)
        else if (singles.node.isPassive())
            throwIllegalState(
                "singlesLoss is pointing to node with key: ${singles.node.item} which is passive.",
                heapRecord)
        else if (singles.node.loss == null)
            throwIllegalState(
                "singlesLoss is pointing to node with key: ${singles.node.item} which has loss set to null.",
                heapRecord)
        else if (singles.node.loss == 0u)
            throwIllegalState(
                "singlesLoss is pointing to node with key: ${singles.node.item} which has loss set to 0.",
                heapRecord)
    }

    // check ranks pointers
    var currRank: RankListRecord<T>? = heapRecord.rankList
    while (currRank != null) {
        checkRankPointers(currRank, heapRecord)
        currRank = currRank.inc
    }

    //    printFixList(heapRecord)
}
