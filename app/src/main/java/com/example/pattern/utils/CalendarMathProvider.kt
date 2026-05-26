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
     * Constants for the infinite pager to ensure smooth scrolling in both directions.
     */
    const val WEEK_PAGER_PIVOT = 25000
    const val DAY_PAGER_PIVOT = WEEK_PAGER_PIVOT * 7

    /**
     * Calculates the Monday of the week for a given pivot date and a page offset.
     * Each page represents one week.
     */
    fun getMondayOfWeek(pivot: LocalDate, pageOffset: Int): LocalDate {
        val pivotMonday = pivot.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return pivotMonday.plusWeeks(pageOffset.toLong())
    }

    /**
     * Generates a list of 7 dates for the week starting from the provided Monday.
     */
    fun getDatesForWeek(monday: LocalDate): List<LocalDate> {
        return (0..6).map { monday.plusDays(it.toLong()) }
    }

    /**
     * Computes the number of weeks (pages) between the pivot's week and the target date's week.
     */
    fun computePageOffset(pivot: LocalDate, targetDate: LocalDate): Int {
        val pivotMonday = pivot.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val targetMonday = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return ChronoUnit.WEEKS.between(pivotMonday, targetMonday).toInt()
    }

    /**
     * Computes the absolute page index for a week based on the pivot.
     */
    fun getWeekPageIndex(pivot: LocalDate, targetDate: LocalDate): Int {
        return WEEK_PAGER_PIVOT + computePageOffset(pivot, targetDate)
    }

    /**
     * Computes the absolute page index for a day based on the pivot.
     */
    fun getDayPageIndex(pivot: LocalDate, targetDate: LocalDate): Int {
        val daysBetween = ChronoUnit.DAYS.between(
            pivot.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
            targetDate
        )
        return DAY_PAGER_PIVOT + daysBetween.toInt()
    }

    /**
     * Gets the date from a day page index.
     */
    fun getDateFromDayIndex(pivot: LocalDate, dayIndex: Int): LocalDate {
        val daysFromPivot = (dayIndex - DAY_PAGER_PIVOT).toLong()
        val pivotMonday = pivot.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return pivotMonday.plusDays(daysFromPivot)
    }
}
