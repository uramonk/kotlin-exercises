package functional.collections.partitions

import org.junit.Test
import kotlin.test.assertEquals

fun <T> Collection<T>.partitions(): Set<Set<Set<T>>> {
    if (this.size <= 1) return setOf(setOf(this.toSet()))
    val n = this.first()
    val list = this.drop(1)
    val set = list.partitions()

    val whereHeadIsAlone: Set<Set<Set<T>>> = set
        .map { it + setOf(setOf(n)) }
        .toSet()
    val whereHeadIsNotAlone: Set<Set<Set<T>>> = set
        .flatMap { partition: Set<Set<T>> ->
            partition.map { subset: Set<T> ->
                partition
                    .minusElement(subset)
                    .plusElement(subset + n)
            }
        }
        .toSet()
    return whereHeadIsAlone + whereHeadIsNotAlone
}

class PartitionsTest {
    @Test
    fun `partitions of two elements`() {
        val result = setOf(1, 2).partitions()
        val expected = setOf(
            setOf(setOf(1), setOf(2)),
            setOf(setOf(1, 2))
        )
        assertEquals(expected, result)
    }

    @Test
    fun `partitions of three elements`() {
        val result = listOf("A", "B", "C").partitions()
        val expected = setOf(
            setOf(setOf("A"), setOf("B"), setOf("C")),
            setOf(setOf("A"), setOf("B", "C")),
            setOf(setOf("A", "B"), setOf("C")),
            setOf(setOf("A", "C"), setOf("B")),
            setOf(setOf("A", "B", "C"))
        )
        assertEquals(expected, result)
    }

    @Test
    fun `partitions of four elements`() {
        val result = listOf("A", "B", "C", "D").partitions()
        val expected = setOf(
            setOf(setOf("D"), setOf("C"), setOf("B"), setOf("A")),
            setOf(setOf("D", "C"), setOf("B"), setOf("A")),
            setOf(setOf("C"), setOf("D", "B"), setOf("A")),
            setOf(setOf("D"), setOf("C", "B"), setOf("A")),
            setOf(setOf("D", "C", "B"), setOf("A")),
            setOf(setOf("C"), setOf("B"), setOf("D", "A")),
            setOf(setOf("D"), setOf("B"), setOf("C", "A")),
            setOf(setOf("D"), setOf("C"), setOf("B", "A")),
            setOf(setOf("B"), setOf("D", "C", "A")),
            setOf(setOf("D", "C"), setOf("B", "A")),
            setOf(setOf("D", "B"), setOf("C", "A")),
            setOf(setOf("C"), setOf("D", "B", "A")),
            setOf(setOf("C", "B"), setOf("D", "A")),
            setOf(setOf("D"), setOf("C", "B", "A")),
            setOf(setOf("D", "C", "B", "A"))
        )
        assertEquals(expected, result)
    }
}
