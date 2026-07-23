package com.example.pattern.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class HabitDailyStateTest {

    @Test
    fun `calculateTotalTimeMs should never return negative when now is before activeSessionStartMs`() {
        val activeStart = 1000L
        val now = 500L // 500ms before start
        
        val state = HabitDailyState(
            habitId = 1,
            date = "2025-01-01",
            accumulatedTimeMs = 2000L,
            activeSessionStartMs = activeStart
        )
        
        val totalTime = state.calculateTotalTimeMs(now)
        
        // Should be exactly accumulatedTimeMs since currentSession should be coerced to 0
        assertEquals(2000L, totalTime)
    }

    @Test
    fun `calculateTotalTimeMs should return accumulated plus elapsed when now is after activeSessionStartMs`() {
        val activeStart = 1000L
        val now = 1500L // 500ms after start
        
        val state = HabitDailyState(
            habitId = 1,
            date = "2025-01-01",
            accumulatedTimeMs = 2000L,
            activeSessionStartMs = activeStart
        )
        
        val totalTime = state.calculateTotalTimeMs(now)
        
        assertEquals(2500L, totalTime)
    }
}
