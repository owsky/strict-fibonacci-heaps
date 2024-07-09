package heaps.strict_fibonacci_heap

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.common.runBlocking
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import utils.generateIDs

class StrictFibonacciHeapTest :
    DescribeSpec({
        describe("filling up and emptying out the heap") {
            it("should process all jobs correctly") {
                shouldNotThrow<Exception> {
                    runBlocking {
                        val jobs =
                            (1..100).map {
                                launch(Dispatchers.Default) {
                                    val nums = generateIDs(100000, 0..10000000, 1234L)
                                    val h = StrictFibonacciHeap(nums)
                                    val extracted = ArrayList<Int>()
                                    for (i in nums.indices) extracted.add(h.extractMin())

                                    val sortedNums = nums.sorted()
                                    sortedNums shouldBe extracted
                                }
                            }

                        jobs.joinAll()
                    }
                }
            }
        }
    })
