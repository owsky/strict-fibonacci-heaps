package heaps.strict_fibonacci_heap.utils

import heaps.strict_fibonacci_heap.auxiliary_structures.HeapRecord
import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord

// merge the queues from h1 and h2, with v in-between them
// return the pointer to the head of h1's queue as the new queue
fun <T : Comparable<T>> mergeQueues(
    h1: HeapRecord<T>,
    v: NodeRecord<T>,
    h2: HeapRecord<T>
): NodeRecord<T> {
    val h1QueueHead = h1.qHead
    val h2QueueHead = h2.qHead

    if (h1QueueHead == null && h2QueueHead == null) {
        // if both queues are empty
        h1.qHead = v
        v.qNext = v
        v.qPrev = v
    } else if (h1QueueHead == null) {
        // if h1 queue is empty
        val h2QueueTail = h2QueueHead!!.qPrev!!
        h1.qHead = v
        v.qNext = h2QueueHead
        v.qPrev = h2QueueTail
        h2QueueHead.qPrev = v
        h2QueueTail.qNext = v
    } else if (h2QueueHead == null) {
        // if h2 queue is empty
        val h1QueueTail = h1QueueHead.qPrev!!
        h1QueueTail.qNext = v
        v.qNext = h1QueueHead
        v.qPrev = h1QueueTail
        h1QueueHead.qPrev = v
    } else {
        // if neither is empty
        val h1QueueTail = h1QueueHead.qPrev!!
        val h2QueueTail = h2QueueHead.qPrev!!
        h1QueueTail.qNext = v
        v.qPrev = h1QueueTail
        h2QueueTail.qPrev = v
        v.qNext = h2QueueTail
        h1QueueHead.qPrev = h2QueueTail
        h2QueueTail.qNext = h1QueueHead
    }
    return h1.qHead!!
}

// remove x from the queue
fun <T : Comparable<T>> removeFromQueue(x: NodeRecord<T>, heapRecord: HeapRecord<T>) {
    val next = x.qNext!!
    val prev = x.qPrev!!

    if (x === next && x === prev) {
        // x is the only item in the queue
        heapRecord.qHead = null
    } else if (next === prev) {
        // there are exactly two items in the queue
        if (heapRecord.qHead === x) heapRecord.qHead = x.qNext
        next.qNext = next
        next.qPrev = next
    } else {
        // there are more than two items in the queue
        if (heapRecord.qHead === x) heapRecord.qHead = x.qNext
        next.qPrev = prev
        prev.qNext = next
    }

    x.qNext = null
    x.qPrev = null
}
