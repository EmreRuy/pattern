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
    // Go back 26 weeks and align to Monday
    val startDate = today.minusWeeks(26).minusDays((today.dayOfWeek.value - 1).toLong())
    val dayLetters = listOf("M", "T", "W", "T", "F", "S", "S")

    // 53 weeks (371 days) to cover a full year aligned to weeks
    return List(53 * 7) { i ->
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
