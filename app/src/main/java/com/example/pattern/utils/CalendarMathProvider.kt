package com.example.pattern.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * A thread-safe, pure-logic class for date-to-pager-index transformations.
 * This class uses Monday as the start of the week.
 */
object CalendarMathProvider {

    /**
     * Calculates the Monday of the week for a given pivot date and a page offset.
     * Each page represents one week.
     *
     * @param pivot The reference date (anchor).
     * @param pageOffset The number of weeks from the pivot's week.
     * @return The LocalDate representing the Monday of the offset week.
     */
    fun getMondayOfWeek(pivot: LocalDate, pageOffset: Int): LocalDate {
        val pivotMonday = pivot.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return pivotMonday.plusWeeks(pageOffset.toLong())
    }

    /**
     * Generates a list of 7 dates for the week starting from the provided Monday.
     *
     * @param monday The start of the week (must be a Monday).
     * @return A list of 7 LocalDates from Monday to Sunday.
     */
    fun getDatesForWeek(monday: LocalDate): List<LocalDate> {
        return (0..6).map { monday.plusDays(it.toLong()) }
    }

    /**
     * Computes the number of weeks (pages) between the pivot's week and the target date's week.
     *
     * @param pivot The reference date (anchor).
     * @param targetDate The date to calculate the offset for.
     * @return The page offset (weeks) relative to the pivot.
     */
    fun computePageOffset(pivot: LocalDate, targetDate: LocalDate): Int {
        val pivotMonday = pivot.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val targetMonday = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return ChronoUnit.WEEKS.between(pivotMonday, targetMonday).toInt()
    }
}
