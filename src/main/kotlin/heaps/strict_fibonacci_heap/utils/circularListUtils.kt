package heaps.strict_fibonacci_heap.utils

import heaps.strict_fibonacci_heap.auxiliary_structures.NodeRecord

fun <T : Comparable<T>> insertIntoCircularList(
    a: NodeRecord<T>,
    x: NodeRecord<T>,
    b: NodeRecord<T>
) {
    a.right = x
    b.left = x
    x.left = a
    x.right = b
}

fun <T : Comparable<T>> removeFromCircularList(x: NodeRecord<T>) {
    val a = x.left
    val b = x.right
    if (a != null && b != null && a != x) {
        if (a.right !== x || b.left !== x)
            throw RuntimeException("The left and right pointers are mismatched")
        a.right = b
        b.left = a
    }
    x.left = null
    x.right = null
}

fun <T : Comparable<T>> siblingsListCut(x: NodeRecord<T>) {
    if (x.left == null || x.right == null) return
    val a = x.left!!
    val b = x.right!!
    val p = a.parent!!

    when {
        // case 1: there is only one child
        x === a && x === b -> {
            p.leftChild = null
        }

        // case 2: there are exactly two children
        a === b -> {
            p.leftChild = a
            a.left = a
            a.right = a
        }

        // case 3: there are more than two children
        else -> {
            a.right = b
            b.left = a
            if (p.leftChild === x) p.leftChild = b
        }
    }
    x.left = null
    x.right = null
}

fun <T : Comparable<T>> siblingsMoveToFirst(x: NodeRecord<T>) {
    val parent = x.parent!!
    val firstChild = parent.leftChild!!
    val lastChild = firstChild.left!!

    // if x is already the first child, do nothing
    if (firstChild === x) return

    siblingsListCut(x)
    insertIntoCircularList(lastChild, x, firstChild)

    parent.leftChild = x
}

fun <T : Comparable<T>> siblingsMoveToLast(x: NodeRecord<T>) {
    val parent = x.parent!!
    val firstChild = parent.leftChild!!
    val lastChild = firstChild.left!!

    // if x is both the first and the last child, then x is the only child, do nothing
    if (firstChild === x && lastChild === x) return

    siblingsListCut(x)
    insertIntoCircularList(lastChild, x, firstChild)
}
