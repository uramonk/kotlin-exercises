package essentials.enums.dayofweek

enum class DayOfWeek(val isWeekend: Boolean, val dayName: String) {
    MONDAY(false, "Monday"),
    TUESDAY(false, "Tuesday"),
    WEDNESDAY(false, "Wednesday"),
    THURSDAY(false, "Thursday"),
    FRIDAY(false, "Friday"),
    SATURDAY(true, "Saturday"),
    SUNDAY(true, "sunday");

    fun nextDay(): DayOfWeek {
        val nextNum = if (ordinal + 1 == DayOfWeek.entries.size) 0 else ordinal + 1
        return DayOfWeek.entries[nextNum]
    }
}

fun main() {
    val friday: DayOfWeek = DayOfWeek.FRIDAY
    println(friday.dayName) // Friday
    println(friday.isWeekend) // false
    val saturday: DayOfWeek = friday.nextDay()
    println(saturday.dayName) // Saturday
    println(saturday.isWeekend) // true
}
