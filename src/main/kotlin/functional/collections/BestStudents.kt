package functional.collections.beststudents

import org.junit.Test
import kotlin.test.assertEquals

fun List<Student>.makeBestStudentsList(): String = this
    .filter { it.pointsInSemester >= 30 && it.result >= 80 }
    .sortedByDescending { it.result }
    .take(10)
    .withIndex()
    .map {
        when {
            it.index == 0 -> it to 5000
            it.index <= 3 -> it to 3000
            else -> it to 1000
        }
    }
    .sortedWith(compareBy({ it.first.value.surname }, { it.first.value.name }))
    .joinToString(separator = "\n") { "${it.first.value.name} ${it.first.value.surname}, \$${it.second}" }

fun List<Student>.makeBestStudentsList2(): String = this
    .filter { it.pointsInSemester >= 30 && it.result >= 80 }
    .sortedByDescending { it.result }
    .zip(INTERNSHIPS)
    .sortedWith(compareBy({ it.first.surname }, { it.first.name }))
    .joinToString(separator = "\n") { (student, internship) ->
        "${student.name} ${student.surname}, $$internship"
    }

private val INTERNSHIPS =
    List(1) { 5_000 } + List(3) { 3_000 } + List(6) { 1_000 }


class Student(
    val name: String,
    val surname: String,
    val result: Double,
    val pointsInSemester: Int
)

class BestStudentsListTest {
    val internshipStudent = Student("Marc", "Smith", 87.0, 32)
    val studentWithTooLowResultToInternship = Student("Marcus", "Smith", 37.0, 32)
    val studentWithNotEnoughPointsForInternship = Student("Marcello", "Smith", 87.0, 12)
    val studentNotPassingBecauseOfResult = Student("Peter", "Jackson", 21.0, 24)
    val studentNotPassingBecauseOfPoints = Student("Michael", "Angelo", 71.0, 12)

    val allStudents = listOf(
        internshipStudent,
        studentWithTooLowResultToInternship,
        studentWithNotEnoughPointsForInternship,
        studentNotPassingBecauseOfResult,
        Student("Noely", "Peterson", 91.0, 22),
        studentNotPassingBecauseOfPoints,
        Student("Noe", "Samson", 41.0, 18),
        Student("Timothy", "Johnson", 51.0, 15),
        Student("Noe", "Peterson", 91.0, 22),
        Student("Ester", "Adams", 81.0, 30),
        Student("Dior", "Angel", 88.5, 38),
        Student("Naja", "Marcson", 100.0, 31),
        Student("Oregon", "Dart", 85.5, 30),
        Student("Ron", "Peters", 89.0, 31),
        Student("Harry", "Potter", 80.0, 30),
        Student("Sansa", "Stark", 49.5, 14),
        Student("Jamme", "Lannister", 80.0, 30),
        Student("Alex", "Nolan", 86.0, 33),
        Student("Jon", "Johnson", 85.1, 31),
        Student("James", "Johnson", 85.2, 31),
        Student("Jack", "Johnson", 85.3, 31)
    )

    @Test
    fun `Single student that matches criteria gets biggest internship`() {
        val text = listOf(internshipStudent).makeBestStudentsList()
        val expected = "Marc Smith, \$5000"
        assertEquals(expected, text)
    }

    @Test
    fun `Single student with too low result doesn't get internship`() {
        val text = listOf(studentWithTooLowResultToInternship).makeBestStudentsList()
        assertEquals("", text)
    }

    @Test
    fun `Result 80 is acceptable`() {
        val student = Student("Noely", "Peterson", 80.0, 32)
        val text = listOf(student).makeBestStudentsList()
        assertEquals("Noely Peterson, \$5000", text)
    }

    @Test
    fun `30 points is acceptable`() {
        val student = Student("Noely", "Peterson", 81.0, 30)
        val text = listOf(student).makeBestStudentsList()
        assertEquals("Noely Peterson, \$5000", text)
    }

    @Test
    fun `Single student with not enough doesn't get internship`() {
        val text = listOf(studentWithNotEnoughPointsForInternship).makeBestStudentsList()
        assertEquals("", text)
    }

    @Test
    fun `Complex test`() {
        val text = allStudents.makeBestStudentsList()
        val expected = """
            Ester Adams, ${'$'}1000
            Dior Angel, ${'$'}3000
            Oregon Dart, ${'$'}1000
            Jack Johnson, ${'$'}1000
            James Johnson, ${'$'}1000
            Jon Johnson, ${'$'}1000
            Naja Marcson, ${'$'}5000
            Alex Nolan, ${'$'}1000
            Ron Peters, ${'$'}3000
            Marc Smith, ${'$'}3000
        """.trimIndent()
        assertEquals(expected, text)
    }
}
