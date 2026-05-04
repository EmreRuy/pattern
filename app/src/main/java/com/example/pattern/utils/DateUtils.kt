package com.example.pattern.utils

import androidx.compose.runtime.Immutable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Immutable
data class CalendarDayModel(
    val date: LocalDate,
    val dayLetter: String,
    val dayNumber: String,
    val fullDateString: String
)

fun LocalDate.toCalendarDayModel(): CalendarDayModel {
    val locale = Locale.getDefault()
    return CalendarDayModel(
        date = this,
        dayLetter = this.dayOfWeek.getDisplayName(TextStyle.NARROW, locale),
        dayNumber = this.dayOfMonth.toString(),
        fullDateString = this.toString()
    )
}

fun Long.toUiDate(): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(formatter)
}
