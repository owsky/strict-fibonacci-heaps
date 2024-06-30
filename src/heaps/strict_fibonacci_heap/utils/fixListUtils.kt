package heaps.strict_fibonacci_heap.utils

import heaps.strict_fibonacci_heap.auxiliary_structures.FixListRecord

fun <T : Comparable<T>> fixListRemove(x: FixListRecord<T>) {
    val beforeX = x.left
    val afterX = x.right
    beforeX.right = afterX
    afterX.left = beforeX
}

fun <T : Comparable<T>> fixListMove(
    x: FixListRecord<T>,
    left: FixListRecord<T>,
    right: FixListRecord<T>
) {
    // cut x from the list
    fixListRemove(x)

    // paste x between new boundaries
    x.left = left
    x.right = right
    left.right = x
    right.left = x
}
