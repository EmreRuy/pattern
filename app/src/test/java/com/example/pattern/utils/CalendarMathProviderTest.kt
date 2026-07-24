package com.example.pattern.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CalendarMathProviderTest {

    @Test
    @DisplayName("Verify year transition from Dec 31 to Jan 1 correctly identifies week boundaries")
    fun testYearTransition() {
        val dec31 = LocalDate.of(2023, 12, 31) // Sunday
        val jan1 = LocalDate.of(2024, 1, 1)    // Monday (Anchor)
        
        val offsetDec = CalendarMathProvider.getWeekPageIndex(dec31)
        val offsetJan = CalendarMathProvider.getWeekPageIndex(jan1)
        
        assertThat(offsetDec).isEqualTo(-1)
        assertThat(offsetJan).isEqualTo(0)
        
        val mondayDec = CalendarMathProvider.getMondayOfWeek(offsetDec)
        assertThat(mondayDec).isEqualTo(LocalDate.of(2023, 12, 25))
    }

    @Test
    @DisplayName("Ensure stable indexing for far future dates")
    fun testFarFuture() {
        val farFuture = LocalDate.of(2050, 1, 1)
        val offset = CalendarMathProvider.getWeekPageIndex(farFuture)
        
        val retrievedMonday = CalendarMathProvider.getMondayOfWeek(offset)
        val expectedMonday = farFuture.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        
        assertThat(retrievedMonday).isEqualTo(expectedMonday)
        assertThat(retrievedMonday.dayOfWeek).isEqualTo(java.time.DayOfWeek.MONDAY)
    }

    @Test
    @DisplayName("Verify day-to-index and index-to-day symmetry")
    fun testDaySymmetry() {
        val targetDate = LocalDate.of(2025, 6, 15)
        val index = CalendarMathProvider.getDayPageIndex(targetDate)
        val resultDate = CalendarMathProvider.getDateFromDayIndex(index)
        
        assertThat(resultDate).isEqualTo(targetDate)
    }

    @Test
    @DisplayName("Verify week-to-index symmetry")
    fun testWeekSymmetry() {
        val targetDate = LocalDate.of(2025, 6, 15) // Sunday
        val index = CalendarMathProvider.getWeekPageIndex(targetDate)
        val resultMonday = CalendarMathProvider.getMondayOfWeek(index)
        
        assertThat(resultMonday).isEqualTo(LocalDate.of(2025, 6, 9))
    }
}
