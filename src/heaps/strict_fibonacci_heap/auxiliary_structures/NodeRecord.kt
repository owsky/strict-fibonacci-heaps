package heaps.strict_fibonacci_heap.auxiliary_structures

class NodeRecord<T : Comparable<T>>(val item: T) {
    var left: NodeRecord<T>? = null
    var right: NodeRecord<T>? = null
    var parent: NodeRecord<T>? = null
    var leftChild: NodeRecord<T>? = null
    var active: ActiveRecord? = null
    var qPrev: NodeRecord<T>? = null
    var qNext: NodeRecord<T>? = null
    var loss: UInt? = null
    var rank: RankWrapper? = null

    inner class RankWrapper {
        var rankListRecord: RankListRecord<T>? = null
            private set

        var fixListRecord: FixListRecord<T>? = null
            private set

        fun setRankRecord(rankRecord: RankListRecord<T>?) {
            if (rankRecord != null) {
                fixListRecord = null
            }
            this.rankListRecord = rankRecord
        }

        fun setFixListRecord(fixListRecord: FixListRecord<T>?) {
            if (fixListRecord != null) {
                rankListRecord = null
            }
            this.fixListRecord = fixListRecord
        }

        override fun toString(): String {
            return when {
                rankListRecord != null -> "RankRecord: $rankListRecord"
                fixListRecord != null -> "FixListRecord: $fixListRecord"
                else -> "Rank is null"
            }
        }
    }
}
