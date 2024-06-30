package heaps.strict_fibonacci_heap.auxiliary_structures

class HeapRecord<T : Comparable<T>> {
    var size: Int = 0
    var root: NodeRecord<T>? = null
    var activeRecord: ActiveRecord = ActiveRecord()
    var nonLinkableChild: NodeRecord<T>? = null
    var qHead: NodeRecord<T>? = null
    var rankList: RankListRecord<T>? = null
    var fixList: FixListRecord<T>? = null
    var singles: FixListRecord<T>? = null
}
