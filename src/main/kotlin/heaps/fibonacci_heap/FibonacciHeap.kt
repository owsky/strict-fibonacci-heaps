package heaps.fibonacci_heap

import heaps.MinHeap
import kotlin.math.ln
import kotlin.math.sqrt

// Implementation adapted from https://www.programiz.com/dsa/fibonacci-heap
class FibonacciHeap<T : Comparable<T>>(items: Collection<T> = emptyList()) : MinHeap<T>(items) {
    var min: Node<T>? = null
    private var found: Node<T>? = null
    private val lookup: HashSet<T> = hashSetOf()

    init {
        items.forEach { insert(it) }
    }

    private fun insert(x: Node<T>) {
        if (min == null) {
            min = x
            x.left = min
            x.right = min
        } else {
            x.right = min
            x.left = min!!.left
            min!!.left!!.right = x
            min!!.left = x
            if (x.key < min!!.key) min = x
        }
    }

    override fun insert(item: T) {
        lookup.add(item)
        insert(Node(item))
    }

    override fun extractMin(): T {
        val z = this.min ?: throw IllegalStateException("Heap is empty")

        var c = z.child
        val k = c
        var p: Node<T>?
        if (c != null) {
            do {
                p = c!!.right
                insert(c)
                c.parent = null
                c = p
            } while (c != null && c !== k)
        }
        z.left!!.right = z.right
        z.right!!.left = z.left
        z.child = null
        if (z === z.right) this.min = null
        else {
            this.min = z.right
            this.consolidate()
        }
        lookup.remove(z.key)
        return z.key
    }

    override fun getSize(): Int {
        return lookup.size
    }

    override fun isEmpty(): Boolean {
        return lookup.size == 0
    }

    override fun contains(key: T): Boolean {
        return lookup.contains(key)
    }

    private fun consolidate() {
        val phi = (1 + sqrt(5.0)) / 2
        val dofn = (ln(lookup.size.toDouble()) / ln(phi)).toInt()
        val a = arrayOfNulls<Node<T>>(dofn + 1)
        for (i in 0..dofn) a[i] = null
        var w = min
        if (w != null) {
            var check = min
            do {
                var x = w
                var d = x!!.degree
                while (a[d] != null) {
                    var y = a[d]
                    if (x!!.key > y!!.key) {
                        val temp = x
                        x = y
                        y = temp
                        w = x
                    }
                    fibHeapLink(y, x)
                    check = x
                    a[d] = null
                    d += 1
                }
                a[d] = x
                w = w!!.right
            } while (w != null && w !== check)
            this.min = null
            for (i in 0..dofn) {
                if (a[i] != null) {
                    insert(a[i]!!)
                }
            }
        }
    }

    // Linking operation
    private fun fibHeapLink(y: Node<T>?, x: Node<T>?) {
        y!!.left!!.right = y.right
        y.right!!.left = y.left

        val p = x!!.child
        if (p == null) {
            y.right = y
            y.left = y
        } else {
            y.right = p
            y.left = p.left
            p.left!!.right = y
            p.left = y
        }
        y.parent = x
        x.child = y
        ++x.degree
        y.mark = false
    }

    // Search operation
    private fun find(key: T, c: Node<T>?) {
        if (found != null || c == null) return
        else {
            var temp = c
            do {
                if (key == temp!!.key) found = temp
                else {
                    val k = temp.child
                    find(key, k)
                    temp = temp.right
                }
            } while (temp !== c && found == null)
        }
    }

    private fun find(k: T): Node<T>? {
        found = null
        find(k, this.min)
        return found
    }

    override fun decreaseKey(key: T, smallerKey: T) {
        val x = find(key)
        decreaseKey(x, smallerKey)
    }

    // Decrease key operation
    private fun decreaseKey(x: Node<T>?, k: T) {
        if (k > x!!.key) return
        x.key = k
        val y = x.parent
        if (y != null && x.key < y.key) {
            cut(x, y)
            cascadingCut(y)
        }
        if (x.key < min!!.key) min = x
    }

    // Cut operation
    private fun cut(x: Node<T>, y: Node<T>) {
        x.right!!.left = x.left
        x.left!!.right = x.right

        --y.degree

        x.right = null
        x.left = null
        insert(x)
        x.parent = null
        x.mark = false
    }

    private fun cascadingCut(y: Node<T>) {
        val z = y.parent
        if (z != null) {
            if (!y.mark) y.mark = true
            else {
                cut(y, z)
                cascadingCut(z)
            }
        }
    }
}
