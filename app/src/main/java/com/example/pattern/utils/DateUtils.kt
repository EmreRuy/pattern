package com.example.pattern.utils


import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class CalendarDayModel(
    val date: LocalDate,
    val dayLetter: String,
    val dayNumber: String,
    val fullDateString: String
)

fun generateNext365Days(): List<CalendarDayModel> {
    val today = LocalDate.now()
    val startDate = today.minusDays(180)
    val dayLetters = listOf("M", "T", "W", "T", "F", "S", "S")

    return List(365) { i ->
        val date = startDate.plusDays(i.toLong())
        CalendarDayModel(
            date = date,
            dayLetter = dayLetters[date.dayOfWeek.value - 1],
            dayNumber = date.dayOfMonth.toString(),
            fullDateString = date.toString()
        )
    }
}

fun Long.toUiDate(): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(formatter)
}
