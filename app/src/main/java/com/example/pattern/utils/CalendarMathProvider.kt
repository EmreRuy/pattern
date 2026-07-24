package com.example.pattern.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * A thread-safe, pure-logic class for date-to-pager-index transformations.
 * 
 * Logic Optimization:
 * Instead of using a dynamic pivot (which can shift), we use a fixed anchor (2024-01-01).
 * This ensures that a specific date ALWAYS maps to the same page index, 
 * making navigation restoration perfectly seamless.
 */
object CalendarMathProvider {

    /**
     * Fixed anchor date (a Monday) to ensure stable indexing across sessions.
     */
    private val ANCHOR_DATE = LocalDate.of(2024, 1, 1)

    /**
     * Calculates the Monday of the week for a given page offset from the anchor.
     */
    fun getMondayOfWeek(pageOffset: Int): LocalDate {
        return ANCHOR_DATE.plusWeeks(pageOffset.toLong())
    }

    /**
     * Generates a list of 7 dates for the week starting from the provided Monday.
     */
    fun getDatesForWeek(monday: LocalDate): List<LocalDate> {
        return (0..6).map { monday.plusDays(it.toLong()) }
    }

    /**
     * Computes the absolute week page index for a target date.
     */
    fun getWeekPageIndex(targetDate: LocalDate): Int {
        val targetMonday = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return ChronoUnit.WEEKS.between(ANCHOR_DATE, targetMonday).toInt()
    }

    /**
     * Computes the absolute day page index for a target date.
     */
    fun getDayPageIndex(targetDate: LocalDate): Int {
        return ChronoUnit.DAYS.between(ANCHOR_DATE, targetDate).toInt()
    }

    /**
     * Gets the date from a day page index.
     */
    fun getDateFromDayIndex(dayIndex: Int): LocalDate {
        return ANCHOR_DATE.plusDays(dayIndex.toLong())
    }
}
