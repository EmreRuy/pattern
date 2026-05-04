package com.example.pattern.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CalendarMathProviderTest {

    private val pivot = LocalDate.of(2024, 1, 1) // A Monday

    @Test
    @DisplayName("BVA: Verify year transition from Dec 31 to Jan 1 correctly identifies week boundaries")
    fun testYearTransition() {
        val dec31 = LocalDate.of(2023, 12, 31) // Sunday
        val jan1 = LocalDate.of(2024, 1, 1)    // Monday
        
        val offsetDec = CalendarMathProvider.computePageOffset(pivot, dec31)
        val offsetJan = CalendarMathProvider.computePageOffset(pivot, jan1)
        
        assertThat(offsetDec).isEqualTo(-1)
        assertThat(offsetJan).isEqualTo(0)
        
        val mondayDec = CalendarMathProvider.getMondayOfWeek(pivot, offsetDec)
        assertThat(mondayDec).isEqualTo(LocalDate.of(2023, 12, 25))
    }

    @Test
    @DisplayName("BVA: Ensure no integer overflow or logic errors for dates in the far future (2050)")
    fun testFarFuture() {
        val farFuture = LocalDate.of(2050, 1, 1) // Wednesday
        val offset = CalendarMathProvider.computePageOffset(pivot, farFuture)
        
        val retrievedMonday = CalendarMathProvider.getMondayOfWeek(pivot, offset)
        val expectedMonday = LocalDate.of(2049, 12, 27)
        
        assertThat(retrievedMonday).isEqualTo(expectedMonday)
    }

    @Test
    @DisplayName("BVA: Ensure no integer overflow or logic errors for dates in the far past (1990)")
    fun testFarPast() {
        val farPast = LocalDate.of(1990, 1, 1) // Monday
        val offset = CalendarMathProvider.computePageOffset(pivot, farPast)
        
        val retrievedMonday = CalendarMathProvider.getMondayOfWeek(pivot, offset)
        assertThat(retrievedMonday).isEqualTo(farPast)
    }

    @Test
    @DisplayName("Equivalence Partitioning: Verify any day within a specific week (Tue, Wed, Sun) correctly maps to the same Monday")
    fun testEquivalencePartitioning() {
        val tuesday = LocalDate.of(2024, 1, 2)
        val wednesday = LocalDate.of(2024, 1, 3)
        val sunday = LocalDate.of(2024, 1, 7)
        
        val offsetTue = CalendarMathProvider.computePageOffset(pivot, tuesday)
        val offsetWed = CalendarMathProvider.computePageOffset(pivot, wednesday)
        val offsetSun = CalendarMathProvider.computePageOffset(pivot, sunday)
        
        assertThat(offsetTue).isEqualTo(0)
        assertThat(offsetWed).isEqualTo(0)
        assertThat(offsetSun).isEqualTo(0)
    }

    @Test
    @DisplayName("Leap Year Stress Test: Verify week alignment remains perfect through Feb 29 of 2028, 2032, and 2036")
    fun testLeapYears() {
        val leapYears = listOf(2028, 2032, 2036)
        
        leapYears.forEach { year ->
            val feb28 = LocalDate.of(year, 2, 28)
            val feb29 = LocalDate.of(year, 2, 29)
            val mar1 = LocalDate.of(year, 3, 1)
            
            val offset28 = CalendarMathProvider.computePageOffset(pivot, feb28)
            val offset29 = CalendarMathProvider.computePageOffset(pivot, feb29)
            val offset01 = CalendarMathProvider.computePageOffset(pivot, mar1)
            
            val monday28 = CalendarMathProvider.getMondayOfWeek(pivot, offset28)
            val monday29 = CalendarMathProvider.getMondayOfWeek(pivot, offset29)
            val monday01 = CalendarMathProvider.getMondayOfWeek(pivot, offset01)
            
            if (feb29.dayOfWeek.value == 1) { // Monday
                 assertThat(monday29).isEqualTo(feb29)
            } else {
                 assertThat(monday29).isEqualTo(monday28)
            }
            
            // mar 1 is either in the same week as feb 29 or the next week
            if (mar1.dayOfWeek.value == 1) { // mar 1 is Monday
                assertThat(offset01).isEqualTo(offset29 + 1)
            } else {
                assertThat(monday01).isEqualTo(monday29)
            }
        }
    }

    @Test
    @DisplayName("Symmetry Test: Verify computePageOffset followed by getMondayOfWeek returns the original week's start date")
    fun testSymmetry() {
        val targetDate = LocalDate.of(2035, 8, 22) // Wednesday
        val offset = CalendarMathProvider.computePageOffset(pivot, targetDate)
        val resultMonday = CalendarMathProvider.getMondayOfWeek(pivot, offset)
        
        val expectedMonday = LocalDate.of(2035, 8, 20)
        assertThat(resultMonday).isEqualTo(expectedMonday)
    }

    @Test
    @DisplayName("Idempotency Test: Verify result remains the same regardless of repeated calls with same input")
    fun testIdempotency() {
        val targetDate = LocalDate.of(2024, 12, 25)
        
        val firstResult = CalendarMathProvider.computePageOffset(pivot, targetDate)
        val secondResult = CalendarMathProvider.computePageOffset(pivot, targetDate)
        
        assertThat(firstResult).isEqualTo(secondResult)
        
        val firstMonday = CalendarMathProvider.getMondayOfWeek(pivot, firstResult)
        val secondMonday = CalendarMathProvider.getMondayOfWeek(pivot, firstResult)
        
        assertThat(firstMonday).isEqualTo(secondMonday)
    }

    @Test
    @DisplayName("Midnight/System Clock Resilience: Ensure logic does not rely on LocalDate.now() internally")
    fun testClockResilience() {
        val testMonday = LocalDate.of(2024, 5, 13)
        val dates = CalendarMathProvider.getDatesForWeek(testMonday)
        
        assertThat(dates).hasSize(7)
        assertThat(dates[0]).isEqualTo(testMonday)
        assertThat(dates[6]).isEqualTo(testMonday.plusDays(6))
    }
}
