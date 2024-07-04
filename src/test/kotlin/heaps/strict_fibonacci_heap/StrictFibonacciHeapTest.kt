package heaps.strict_fibonacci_heap

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class StrictFibonacciHeapTest :
    DescribeSpec({
        describe("Strict Fibonacci Heap") {
            it("should create an empty heap") {
                val heap = StrictFibonacciHeap<Int>()
                heap.getSize() shouldBe 0
                heap.heapRecord.root shouldBe null
            }

            describe("insert tests") {
                it("should add element 4 and trigger a root degree reduction") {
                    val nums = intArrayOf(1, 2, 3).toList()
                    val h = StrictFibonacciHeap(nums)
                    h.insert(4)

                    h.heapRecord.root shouldNotBe null
                    h.heapRecord.root!!.item shouldBe 1

                    h.heapRecord.root!!.leftChild shouldNotBe null
                    val firstChild = h.heapRecord.root!!.leftChild!!
                    firstChild.item shouldBe 2
                    firstChild.getRank().rankNumber shouldBe 1
                    firstChild.isActiveRoot() shouldBe true

                    val secondChild = firstChild.leftChild
                    secondChild shouldNotBe null
                    secondChild!!.item shouldBe 3
                    secondChild.getRank().rankNumber shouldBe 0

                    val thirdChild = secondChild.leftChild
                    thirdChild shouldNotBe null
                    thirdChild!!.item shouldBe 4
                    thirdChild.isPassive() shouldBe true
                }
            }
        }
    })
