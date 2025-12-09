package com.example.pattern.utils


import java.time.LocalDate

fun generateNext365Days(): List<Pair<String, String>> {
    val today = LocalDate.now()
    val startDate = today.minusDays(180)

    val dayLetters = listOf("M", "T", "W", "T", "F", "S", "S")

    return List(365) { i ->
        val date = startDate.plusDays(i.toLong())
        val letter = dayLetters[date.dayOfWeek.value - 1]
        val number = date.dayOfMonth.toString()
        letter to number
    }
}
