package heaps.strict_fibonacci_heap.auxiliary_structures

class FixListRecord<T : Comparable<T>>(val node: NodeRecord<T>, var rank: RankListRecord<T>) {
    var left: FixListRecord<T>? = null
    var right: FixListRecord<T>? = null

    fun forEach(action: (FixListRecord<T>) -> Unit) {
        val visited = mutableSetOf<FixListRecord<T>>()
        var current: FixListRecord<T>? = this

        while (current != null && current !in visited) {
            action(current)
            visited.add(current)
            current = current.right
        }
    }

    fun isInPartOne(): Boolean {
        if (left == null || right == null || rank.activeRoots == null) return false

        if (rank.isActiveRootTransformable()) {
            // if rank is active root transformable, then the node to the left needs to have the
            // same rank as this, or the activeRoots pointer needs to be pointing at this
            return left!!.rank === this.rank || rank.activeRoots === this
        }
        return false
    }

    fun isInPartTwo(): Boolean {
        if (left == null || right == null || rank.activeRoots == null) return false

        if (!rank.isActiveRootTransformable()) {
            // if rank is not active root transformable, then the node to the left needs to have a
            // different rank and the node to the right needs to either have a different rank if
            // it's an active root, or it needs to be a node with positive loss
            return left!!.rank !== rank && (right!!.rank !== rank || !right!!.node.isActiveRoot())
        }

        return false
    }

    fun isInPartThree(): Boolean {
        if (left == null || right == null || rank.loss == null) return false

        if (!rank.isLossTransformable()) {
            // if rank is not loss transformable, then the node to the left needs to be either an
            // active root or a positive loss node with a different rank. The node to the right
            // needs to be a positive node with a different rank
            return (left!!.node.isActiveRoot() || left!!.rank !== this.rank) &&
                right!!.rank !== this.rank
        }

        return false
    }

    fun isInPartFour(): Boolean {
        if (left == null || right == null || rank.loss == null) return false

        if (rank.isLossTransformable()) {
            // if rank is loss transformable, then the node to the right needs to have the same rank
            // as this or the rank's loss pointer needs to be pointing at this
            return right!!.rank === this.rank || this.rank.loss === this
        }
        return false
    }
}
