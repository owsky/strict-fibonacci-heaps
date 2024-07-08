package utils

import kotlin.random.Random

fun generateIDs(count: Int, range: IntRange, seed: Long): Set<Int> {
    if (range.last - range.first < count) throw IllegalArgumentException("Range is too narrow")
    val random = Random(seed)
    val uniqueIntegers = mutableSetOf<Int>()

    while (uniqueIntegers.size < count) uniqueIntegers.add(random.nextInt(range.first, range.last))

    return uniqueIntegers
}
