package heaps.strict_fibonacci_heap.auxiliary_structures

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

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

    var fixListSize = 0
        private set

    init {
        root?.let {
            val newNode = NodeRecord(root)
            this.root = newNode
            size = 1
        }
    }

    private fun getFixListPartHead(fixListPart: FixListPart): FixListRecord<T>? {
        return when (fixListPart) {
            FixListPart.PART_ONE -> fixListPartOne
            FixListPart.PART_TWO -> fixListPartTwo
            FixListPart.PART_THREE -> fixListPartThree
            FixListPart.PART_FOUR -> fixListPartFour
        }
    }

    private fun setFixListPartHead(xFix: FixListRecord<T>) {
        when (xFix.fixListPart) {
            FixListPart.PART_ONE -> fixListPartOne = xFix
            FixListPart.PART_TWO -> fixListPartTwo = xFix
            FixListPart.PART_THREE -> fixListPartThree = xFix
            FixListPart.PART_FOUR -> fixListPartFour = xFix
            else -> throw IllegalStateException("Node is not on the fix-list, can't set head")
        }
    }

    private fun removeFixListPartHead(fixListPart: FixListPart) {
        when (fixListPart) {
            FixListPart.PART_ONE -> fixListPartOne = null
            FixListPart.PART_TWO -> fixListPartTwo = null
            FixListPart.PART_THREE -> fixListPartThree = null
            FixListPart.PART_FOUR -> fixListPartFour = null
        }
    }

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

    private fun fixListInsertIntoPartOne(xFix: FixListRecord<T>) {
        logger.debug { "Inserting ${xFix.node.item} into part one of the fix-list" }
        fixListInsertHead(xFix, FixListPart.PART_ONE)
    }

    private fun fixListInsertIntoPartTwo(xFix: FixListRecord<T>) {
        logger.debug { "Inserting ${xFix.node.item} into part two of the fix-list" }
        fixListInsertHead(xFix, FixListPart.PART_TWO)
    }

    private fun fixListInsertIntoPartThree(xFix: FixListRecord<T>) {
        logger.debug { "Inserting ${xFix.node.item} into part three of the fix-list" }
        fixListInsertHead(xFix, FixListPart.PART_THREE)
    }

    private fun fixListInsertIntoPartFour(xFix: FixListRecord<T>) {
        logger.debug { "Inserting ${xFix.node.item} into part four of the fix-list" }
        fixListInsertHead(xFix, FixListPart.PART_FOUR)
    }

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
                val rankActiveRoots = xRank.activeRoots
                if (rankActiveRoots == null) {
                    xRank.setActiveRootsPointer(xFix)
                    fixListInsertIntoPartTwo(xFix)
                } else {
                    val nextInFix = rankActiveRoots.right
                    if (nextInFix != null &&
                        nextInFix.node.loss == 0u &&
                        nextInFix.rank === xRank) {
                        fixListInsertRightOf(xFix, rankActiveRoots)
                    } else {
                        fixListRemove(rankActiveRoots, false)
                        fixListInsertIntoPartOne(rankActiveRoots)
                        fixListInsertRightOf(xFix, rankActiveRoots)
                    }
                    xFix.fixListPart = FixListPart.PART_ONE
                }
            }

            else -> {
                val rankLoss = xRank.loss
                if (rankLoss == null) {
                    xRank.setLossPointer(xFix)
                    fixListInsertIntoPartThree(xFix)
                } else {
                    val nextInFix = rankLoss.right
                    if (nextInFix != null &&
                        nextInFix.node.loss!! > 0u &&
                        nextInFix.rank === xRank) {
                        fixListInsertRightOf(xFix, rankLoss)
                    } else {
                        fixListRemove(rankLoss, false)
                        fixListInsertIntoPartFour(rankLoss)
                        fixListInsertRightOf(xFix, rankLoss)
                    }
                    xFix.fixListPart = FixListPart.PART_FOUR
                }
            }
        }

        fixListSize++
    }

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
    }

    private fun fixListInsertRightOf(xFix: FixListRecord<T>, yFix: FixListRecord<T>) {
        if (yFix.right === xFix) return

        val nextInFix = yFix.right

        if (nextInFix == null) {
            // there is only one item on the fix-list
            yFix.right = xFix
            xFix.left = yFix
        } else {
            fixListInsertBetween(xFix = xFix, left = yFix, right = nextInFix)
        }
    }

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

    fun fixListRemove(xFix: FixListRecord<T>, resetRankPointers: Boolean) {
        logger.debug { "Removing ${xFix.node.item} from the fix-list" }
        if (resetRankPointers) {
            xFix.rank.removeFixListPointers(xFix, this)
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
                // if xFix was an active root, check if activeRoots needs moving
                xFix.rank.activeRoots?.let { activeRoots ->
                    val nextInFix = activeRoots.right
                    if (nextInFix == null || nextInFix.rank !== xFix.rank) {
                        fixListRemove(activeRoots, false)
                        fixListInsertIntoPartTwo(activeRoots)
                    }
                }
            } else {
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

    fun fixListForEach(action: (FixListRecord<T>) -> Unit) {
        for (currentFixPartEnum in FixListPart.values()) {
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
