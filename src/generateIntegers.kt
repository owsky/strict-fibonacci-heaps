import kotlin.random.Random

fun generateIntegers(seed: Long, count: Int, range: IntRange): List<Int> {
    if (range.last - range.first < count) throw IllegalArgumentException("Range is too narrow")
    //    val random = Random(seed)
    val random = Random(Random.nextLong())
    val uniqueIntegers = mutableSetOf<Int>()

    while (uniqueIntegers.size < count) uniqueIntegers.add(
        random.nextInt(range.first, range.last + 1))

    return uniqueIntegers.toList()
}
