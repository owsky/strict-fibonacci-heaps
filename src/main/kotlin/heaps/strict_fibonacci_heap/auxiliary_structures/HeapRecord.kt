package heaps.strict_fibonacci_heap.auxiliary_structures

class HeapRecord<T : Comparable<T>>(root: T? = null) {
    var size: Int = 0
    var root: NodeRecord<T>? = null
    var activeRecord: ActiveRecord = ActiveRecord()
    var nonLinkableChild: NodeRecord<T>? = null
    var qHead: NodeRecord<T>? = null
    var rankList: RankListRecord<T> = RankListRecord(0)
    var fixList: FixListRecord<T>? = null
    var singles: FixListRecord<T>? = null

    init {
        root?.let {
            val newNode = NodeRecord(root)
            this.root = newNode
            size = 1
        }
    }
}
