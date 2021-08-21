package time

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


fun main() {
    println(" ====== Time Parsing Tests ====== ")
    println()

    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    val dateTimes = arrayListOf<LocalDateTime>(
        LocalDateTime.parse("01.03.2021 08:02", formatter),
        LocalDateTime.parse("01.03.2023 08:02", formatter),
        LocalDateTime.parse("01.10.2021 08:02", formatter)
    )

    var count = -1;

    for (dateTime in dateTimes) {
        println("-> Test ${++count}")

        if (dateTime.isBefore(LocalDateTime.now()) || dateTime.isAfter(LocalDateTime.now().plusYears(1))) {
            println("Value cannot be either before the current time, neither in more than one year!")
            println()
            continue
        }

        println(dateTime.toString())
        println()
    }

    println()

    testBrokenDateTime()
}

fun testBrokenDateTime() {
    println(" ====== Broken Time Parsing Tests ====== ")
    println()

    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    val dateTimes = arrayListOf(
        "011.03.2021 08:02",
        "01.034.2023 08:02",
        "01.10.20231 08:02",
        "01.10.2023 080:02"
    )

    var count = -1

    for (dateTime in dateTimes) {
        println("-> Test ${++count}")

        try {
            println(LocalDateTime.parse(dateTime, formatter))
        } catch (exception: DateTimeParseException) {
            println("Invalid Date! Error at: ${makeErrorBold(exception.errorIndex, dateTime)}, index ${exception.errorIndex}")
        }

        println()
    }
}

// Discord only
fun makeErrorBold(errorIndex: Int, string: String): String {
    return string.substring(0, errorIndex) + "**" + string.substring(errorIndex, errorIndex + 1) + "**" + string.substring(errorIndex + 1)
}