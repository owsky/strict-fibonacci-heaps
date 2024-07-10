package heaps.strict_fibonacci_heap.auxiliary_structures

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * This class defines the Heap Record, which serves as the entrypoint for a Strict Fibonacci Heap.
 * It holds references to key parts of the heap, like the [root] and the [activeRecord].
 */
class HeapRecord<T : Comparable<T>>(root: T? = null) {
    var size: Int = 0
    var root: NodeRecord<T>? = null
    var activeRecord: ActiveRecord = ActiveRecord()
    var nonLinkableChild: NodeRecord<T>? = null
    var qHead: NodeRecord<T>? = null
    var rankList: RankListRecord<T> = RankListRecord(0)

    var fixListPartOne: FixListRecord<T>? = null
        private set

    var fixListPartTwo: FixListRecord<T>? = null
        private set

    var fixListPartThree: FixListRecord<T>? = null
        private set

    var fixListPartFour: FixListRecord<T>? = null
        private set

    private var fixListSize = 0

    init {
        root?.let {
            val newNode = NodeRecord(root)
            this.root = newNode
            size = 1
        }
    }

    /**
     * Returns the head of the fix-list for the specified [fixListPart].
     *
     * T(n) = O(1)
     */
    private fun getFixListPartHead(fixListPart: FixListPart): FixListRecord<T>? {
        return when (fixListPart) {
            FixListPart.PART_ONE -> fixListPartOne
            FixListPart.PART_TWO -> fixListPartTwo
            FixListPart.PART_THREE -> fixListPartThree
            FixListPart.PART_FOUR -> fixListPartFour
        }
    }

    /**
     * Sets the head of the fix-list part for which [xFix] is located.
     *
     * T(n) = O(1)
     */
    private fun setFixListPartHead(xFix: FixListRecord<T>) {
        when (xFix.fixListPart) {
            FixListPart.PART_ONE -> fixListPartOne = xFix
            FixListPart.PART_TWO -> fixListPartTwo = xFix
            FixListPart.PART_THREE -> fixListPartThree = xFix
            FixListPart.PART_FOUR -> fixListPartFour = xFix
            else -> throw IllegalStateException("Node is not on the fix-list, can't set head")
        }
    }

    /**
     * Removes the head of the fix-list for the specified [fixListPart].
     *
     * T(n) = O(1)
     */
    private fun removeFixListPartHead(fixListPart: FixListPart) {
        when (fixListPart) {
            FixListPart.PART_ONE -> fixListPartOne = null
            FixListPart.PART_TWO -> fixListPartTwo = null
            FixListPart.PART_THREE -> fixListPartThree = null
            FixListPart.PART_FOUR -> fixListPartFour = null
        }
    }

    /**
     * Inserts the given [xFix] into the fix-list if there are no nodes in any of the four parts.
     *
     * T(n) = O(1)
     */
    private fun insertIntoEmptyFixList(xFix: FixListRecord<T>) {
        if (fixListSize > 0) throw IllegalStateException("Fix-list is not empty")

        logger.debug { "Inserting ${xFix.node.item} into the empty fix-list" }

        fixListSize = 1
        if (xFix.node.loss!! > 0u) {
            fixListPartThree = xFix
            xFix.fixListPart = FixListPart.PART_THREE
            xFix.rank.setLossPointer(xFix)
        } else {
            fixListPartTwo = xFix
            xFix.fixListPart = FixListPart.PART_TWO
            xFix.rank.setActiveRootsPointer(xFix)
        }
    }

    /**
     * Inserts the given [xFix] into part one of the fix-list.
     *
     * T(n) = O(1)
     */
    private fun fixListInsertIntoPartOne(xFix: FixListRecord<T>) {
        logger.debug { "Inserting ${xFix.node.item} into part one of the fix-list" }
        fixListInsertHead(xFix, FixListPart.PART_ONE)
    }

    /**
     * Inserts the given [xFix] into part two of the fix-list.
     *
     * T(n) = O(1)
     */
    private fun fixListInsertIntoPartTwo(xFix: FixListRecord<T>) {
        logger.debug { "Inserting ${xFix.node.item} into part two of the fix-list" }
        fixListInsertHead(xFix, FixListPart.PART_TWO)
    }

    /**
     * Inserts the given [xFix] into part three of the fix-list.
     *
     * T(n) = O(1)
     */
    private fun fixListInsertIntoPartThree(xFix: FixListRecord<T>) {
        logger.debug { "Inserting ${xFix.node.item} into part three of the fix-list" }
        fixListInsertHead(xFix, FixListPart.PART_THREE)
    }

    /**
     * Inserts the given [xFix] into part four of the fix-list.
     *
     * T(n) = O(1)
     */
    private fun fixListInsertIntoPartFour(xFix: FixListRecord<T>) {
        logger.debug { "Inserting ${xFix.node.item} into part four of the fix-list" }
        fixListInsertHead(xFix, FixListPart.PART_FOUR)
    }

    /**
     * Inserts the given node [x] into part the proper part of the fix-list.
     *
     * T(n) = O(1)
     */
    fun insertIntoFixList(x: NodeRecord<T>) {
        if (x.rank is FixListRecord<*>)
            throw IllegalArgumentException(
                "Trying to insert node ${x.item} on the fix-list, but it's already on it")
        logger.debug { "Inserting ${x.item} into the fix-list" }

        val xRank = x.getRank()
        val xFix = FixListRecord(x, xRank)
        x.rank = xFix

        if (fixListSize == 0) {
            insertIntoEmptyFixList(xFix)
            return
        }

        when (x.loss) {
            null -> {
                throw IllegalArgumentException(
                    "Trying to insert node ${x.item} on the fix list, but its loss is null")
            }

            0u -> {
                // if the loss is zero, then xFix will either go in part one or part two
                val rankActiveRoots = xRank.activeRoots
                if (rankActiveRoots == null) {
                    // if xFix's rank doesn't point to an active root, set the pointer to xFix and
                    // insert it into part one
                    xRank.setActiveRootsPointer(xFix)
                    fixListInsertIntoPartTwo(xFix)
                } else {
                    // follow the rank's activeRoots pointer to insert xFix into the proper part
                    val nextInFix = rankActiveRoots.right
                    if (nextInFix != null &&
                        nextInFix.node.loss == 0u &&
                        nextInFix.rank === xRank) {
                        // if the node after activeRoots is an active root of the same rank, it
                        // means that activeRoots is already in part one so simply add xFix to its
                        // right
                        fixListInsertRightOf(xFix, rankActiveRoots)
                    } else {
                        // otherwise activeRoots is in part two, so move it to part one first and
                        // then insert xFix to its right
                        fixListRemove(rankActiveRoots, false)
                        fixListInsertIntoPartOne(rankActiveRoots)
                        fixListInsertRightOf(xFix, rankActiveRoots)
                    }
                }
            }

            else -> {
                // if the loss is non-zero then xFix needs to go either in part three or part four
                val rankLoss = xRank.loss
                if (rankLoss == null) {
                    // if xFix's rank doesn't point to a node with positive loss, set it to xFix
                    xRank.setLossPointer(xFix)
                    // if xFix's loss is greater than 2, insert it into part four
                    if (x.loss!! >= 2u) fixListInsertIntoPartFour(xFix)
                    // otherwise insert it into part three
                    else fixListInsertIntoPartThree(xFix)
                } else {
                    // otherwise follow the rank's loss pointer to insert xFix into the proper part
                    val nextInFix = rankLoss.right
                    if (nextInFix != null &&
                        nextInFix.node.loss!! > 0u &&
                        nextInFix.rank === xRank) {
                        // if the node to the right of the loss is an active node with positive loss
                        // of the same rank, it means that loss is already in part four so add xFix
                        // to its right
                        fixListInsertRightOf(xFix, rankLoss)
                    } else {
                        // otherwise first move loss to part four and then insert xFix to its right
                        fixListRemove(rankLoss, false)
                        fixListInsertIntoPartFour(rankLoss)
                        fixListInsertRightOf(xFix, rankLoss)
                    }
                }
            }
        }

        fixListSize++
    }

    /**
     * Inserts [xFix] in-between [left] and [right].
     *
     * T(n) = O(1)
     */
    private fun fixListInsertBetween(
        xFix: FixListRecord<T>,
        left: FixListRecord<T>,
        right: FixListRecord<T>
    ) {
        if (left === right) throw IllegalArgumentException("Left and right need to be distinct")

        // paste x between new boundaries
        xFix.left = left
        xFix.right = right
        left.right = xFix
        right.left = xFix
        xFix.fixListPart = left.fixListPart
    }

    /**
     * Inserts [xFix] to the right of [yFix].
     *
     * T(n) = O(1)
     */
    private fun fixListInsertRightOf(xFix: FixListRecord<T>, yFix: FixListRecord<T>) {
        if (yFix.right === xFix) return

        val nextInFix = yFix.right
        xFix.fixListPart = yFix.fixListPart

        if (nextInFix == null) {
            // there is only one item on the fix-list
            yFix.right = xFix
            xFix.left = yFix
        } else {
            fixListInsertBetween(xFix = xFix, left = yFix, right = nextInFix)
        }
    }

    /**
     * Inserts [xFix] at the head of the fix-list part, depending on the value of [fixListPart].
     *
     * T(n) = O(1)
     */
    private fun fixListInsertHead(xFix: FixListRecord<T>, fixListPart: FixListPart) {
        if (xFix.fixListPart === fixListPart)
            throw IllegalArgumentException(
                "Trying to insert node ${xFix.node.item} into the fix-list but it's already there")
        xFix.fixListPart = fixListPart

        getFixListPartHead(fixListPart)?.let { firstInFix ->
            xFix.right = firstInFix
            firstInFix.left = xFix
            setFixListPartHead(xFix)
        } ?: run { setFixListPartHead(xFix) }
    }

    /**
     * Removes [xFix] from the fix-list. If [resetRankPointers] is true, then also resets the
     * [xFix]'s rank's pointers. Use [resetRankPointers] set to false if you're moving [xFix] from
     * one part of the fix-list to another. Otherwise, set it to true if you're removing [xFix]
     * permanently from the fix-list.
     */
    fun fixListRemove(xFix: FixListRecord<T>, resetRankPointers: Boolean) {
        logger.debug { "Removing ${xFix.node.item} from the fix-list" }
        if (resetRankPointers) {
            xFix.rank.resetFixListPointers(xFix)
            xFix.node.rank = xFix.node.getRank()
        }

        val prev = xFix.left
        val next = xFix.right

        when {
            prev == null && next == null -> {
                removeFixListPartHead(xFix.fixListPart!!)
            }

            prev == null && next != null -> {
                setFixListPartHead(next)
                next.left = null
            }

            prev != null && next == null -> {
                prev.right = null
            }

            prev != null && next != null -> {
                prev.right = next
                next.left = prev
            }
        }

        xFix.left = null
        xFix.right = null
        xFix.fixListPart = null

        if (resetRankPointers) {
            fixListSize--
            if (xFix.node.loss == 0u) {
                // if xFix was an active root, check if xFix's rank's activeRoots needs moving
                xFix.rank.activeRoots?.let { activeRoots ->
                    val nextInFix = activeRoots.right
                    if (nextInFix == null || nextInFix.rank !== xFix.rank) {
                        fixListRemove(activeRoots, false)
                        fixListInsertIntoPartTwo(activeRoots)
                    }
                }
            } else {
                // if xFix was a node with positive loss, check if xFix's rank's loss needs moving
                xFix.rank.loss?.let { loss ->
                    val nextInFix = loss.right
                    if (nextInFix == null || nextInFix.rank !== xFix.rank) {
                        fixListRemove(loss, false)
                        fixListInsertIntoPartThree(loss)
                    }
                }
            }
        }
    }

    /**
     * Iterates over all nodes on all the fix-list parts, starting from part one. Only used for
     * debugging purposes.
     *
     * T(n) = O(m), with m equal to the number of nodes on the fix-list.
     */
    fun fixListForEach(action: (FixListRecord<T>) -> Unit) {
        for (currentFixPartEnum in FixListPart.entries) {
            val currentFixPart = getFixListPartHead(currentFixPartEnum)
            var currentFix = currentFixPart
            currentFixPart?.let {
                while (currentFix != null) {
                    action(currentFix!!)
                    currentFix = currentFix!!.right
                }
            }
        }
    }
}
