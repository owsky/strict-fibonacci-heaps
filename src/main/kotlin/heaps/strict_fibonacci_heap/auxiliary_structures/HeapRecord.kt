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

    var fixList: FixListRecord<T>? = null
        private set

    var singles: FixListRecord<T>? = null
        private set

    init {
        root?.let {
            val newNode = NodeRecord(root)
            this.root = newNode
            size = 1
        }
    }

    private fun insertIntoEmptyFixList(xFix: FixListRecord<T>) {
        if (fixList != null) throw IllegalStateException("Fix-list is not empty")

        logger.debug { "Inserting ${xFix.node.item} into the empty fix-list" }

        fixList = xFix
        xFix.left = xFix
        xFix.right = xFix
        if (xFix.node.loss!! > 0u) {
            singles = xFix
            xFix.rank.setLossPointer(xFix)
        } else {
            xFix.rank.setActiveRootsPointer(xFix)
        }
    }

    private fun fixListInsertIntoPartOne(xFix: FixListRecord<T>) {
        logger.debug { "Inserting ${xFix.node.item} into part one of the fix-list" }
        if (fixList == null) insertIntoEmptyFixList(xFix) else fixListInsertHead(xFix)
    }

    private fun fixListInsertIntoPartTwo(xFix: FixListRecord<T>) {
        logger.debug { "Inserting ${xFix.node.item} into part two of the fix-list" }

        if (fixList == null) {
            insertIntoEmptyFixList(xFix)
            return
        }

        if (singles != null) {
            fixListInsertLeftOf(xFix, singles!!)
        } else {
            if (fixList!!.node.loss == 0u) {
                fixListInsertTail(xFix)
            } else {
                // maybe bad
                var currentFix = fixList!!
                var started = false
                while (currentFix.left!!.node.loss != 0u && (!started || currentFix !== fixList)) {
                    currentFix = currentFix.left!!
                    started = true
                }
                fixListInsertLeftOf(xFix, currentFix)
            }
        }
    }

    private fun fixListInsertIntoPartThree(xFix: FixListRecord<T>) {
        logger.debug { "Inserting ${xFix.node.item} into part three of the fix-list" }

        if (fixList == null) {
            insertIntoEmptyFixList(xFix)
            return
        }

        if (singles != null) {
            fixListInsertRightOf(xFix, singles!!)
        } else {
            if (fixList!!.node.loss == 0u) {
                fixListInsertTail(xFix)
            } else {
                singles = xFix
                // maybe bad
                var currentFix = fixList!!
                var started = false
                while (currentFix.left!!.node.loss != 0u && (!started || currentFix !== fixList)) {
                    currentFix = currentFix.left!!
                    started = true
                }
                fixListInsertLeftOf(xFix, currentFix)
            }
        }
    }

    private fun fixListInsertIntoPartFour(xFix: FixListRecord<T>) {
        logger.debug { "Inserting ${xFix.node.item} into part four of the fix-list" }

        if (fixList == null) insertIntoEmptyFixList(xFix) else fixListInsertTail(xFix)
    }

    fun insertIntoFixList(x: NodeRecord<T>) {
        if (x.rank is FixListRecord<*>)
            throw IllegalArgumentException(
                "Trying to insert node ${x.item} on the fix-list, but it's already on it")
        logger.debug { "Inserting ${x.item} into the fix-list" }

        val xRank = x.getRank()
        val xFix = FixListRecord(x, xRank)
        x.rank = xFix

        if (fixList == null) {
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
                    val nextInFix = rankActiveRoots.right!!
                    if (nextInFix.node.loss == 0u && nextInFix.rank === xRank) {
                        fixListInsertRightOf(xFix, rankActiveRoots)
                    } else {
                        fixListRemove(rankActiveRoots, false)
                        fixListInsertIntoPartOne(rankActiveRoots)
                        fixListInsertRightOf(xFix, rankActiveRoots)
                    }
                }
            }

            else -> {
                val rankLoss = xRank.loss
                if (rankLoss == null) {
                    xRank.setLossPointer(xFix)
                    fixListInsertIntoPartThree(xFix)
                } else {
                    val prevInFix = rankLoss.left!!
                    if (prevInFix.node.loss!! > 0u && prevInFix.rank === xRank) {
                        fixListInsertLeftOf(xFix, rankLoss)
                    } else {
                        fixListRemove(rankLoss, false)
                        fixListInsertIntoPartFour(rankLoss)
                        fixListInsertLeftOf(xFix, rankLoss)
                    }
                }
            }
        }
    }

    private fun fixListInsertBetween(
        xFix: FixListRecord<T>,
        left: FixListRecord<T>,
        right: FixListRecord<T>
    ) {
        if (left === right) throw IllegalArgumentException("a and b need to be distinct")

        // paste x between new boundaries
        xFix.left = left
        xFix.right = right
        left.right = xFix
        right.left = xFix
    }

    private fun fixListInsertRightOf(xFix: FixListRecord<T>, yFix: FixListRecord<T>) {
        if (yFix.right === xFix) return

        val nextInFix = yFix.right!!

        if (yFix === nextInFix) {
            // there is only one item on the fix-list
            fixListInsertTail(xFix)
        } else {
            fixListInsertBetween(xFix = xFix, left = yFix, right = nextInFix)
        }
    }

    private fun fixListInsertLeftOf(xFix: FixListRecord<T>, yFix: FixListRecord<T>) {
        if (yFix.left === xFix) return

        val prevInFix = yFix.left!!

        if (yFix === prevInFix) {
            // there is only one item on the fix-list
            fixListInsertHead(xFix)
        } else {
            fixListInsertBetween(xFix = xFix, left = prevInFix, right = yFix)
        }
    }

    private fun fixListInsertHead(xFix: FixListRecord<T>) {
        // check if fix-list is empty
        if (this.fixList == null) {
            insertIntoEmptyFixList(xFix)
            return
        }

        val firstInFix = this.fixList!!.right

        // check if xFix is already at the head of the fix-list
        if (firstInFix === xFix) return

        val lastInFix = this.fixList!!

        if (firstInFix === lastInFix) {
            // there is only one item left on the fix-list
            xFix.left = lastInFix
            xFix.right = lastInFix
            lastInFix.left = xFix
            lastInFix.right = xFix
        } else {
            fixListInsertBetween(xFix = xFix, left = lastInFix, right = firstInFix!!)
        }
    }

    private fun fixListInsertTail(xFix: FixListRecord<T>) {
        // check if xFix is already at the tail of the fix-list
        if (this.fixList === xFix) return

        // move xFix to the head of the list
        fixListInsertHead(xFix)

        // move the tail pointer to the head
        this.fixList = xFix
    }

    fun fixListRemove(xFix: FixListRecord<T>, resetRankPointers: Boolean) {
        logger.debug { "Removing ${xFix.node.item} from the fix-list" }
        if (resetRankPointers) {
            xFix.rank.removeFixListPointers(xFix, this)
            xFix.node.rank = xFix.node.getRank()
        }

        // check if singles needs updating
        if (singles === xFix) {
            val nextInFix = xFix.right!!
            singles = if (nextInFix !== xFix && nextInFix.node.loss != 0u) nextInFix else null
        }

        val prev = xFix.left!!
        val next = xFix.right!!

        if (xFix === next) {
            // xFix is the last item on the fix-list
            fixList = null
        } else if (prev === next) {
            // there are only two items left on the fix-list
            fixList = prev
            prev.right = prev
            prev.left = prev
        } else {
            // there are more than two items on the fix-list
            prev.right = next
            next.left = prev
            if (fixList === xFix) fixList = prev
        }

        xFix.left = null
        xFix.right = null

        if (resetRankPointers) {
            if (xFix.node.loss == 0u) {
                // if xFix was an active root, check if activeRoots needs moving
                xFix.rank.activeRoots?.let { activeRoots ->
                    val nextInFix = activeRoots.right!!
                    if (nextInFix.node.loss != 0u || nextInFix.rank !== xFix.rank) {
                        fixListRemove(activeRoots, false)
                        fixListInsertIntoPartTwo(activeRoots)
                    }
                }
            } else {
                xFix.rank.loss?.let { loss ->
                    val prevInFix = loss.left!!
                    if (prevInFix.node.loss == 0u || prevInFix.rank !== xFix.rank) {
                        fixListRemove(loss, false)
                        fixListInsertIntoPartThree(loss)
                    }
                }
            }
        }
    }
}
