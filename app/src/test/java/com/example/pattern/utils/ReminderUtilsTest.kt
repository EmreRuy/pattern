package com.example.pattern.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderUtilsTest {

    private val zoneId = ZoneId.of("UTC")
    private val everyDay = List(7) { true }
    private val weekends = listOf(false, false, false, false, false, true, true)

    @Test
    fun `calculateNextTrigger - when scheduled time is later today - returns today`() {
        // Monday 10:00
        val now = ZonedDateTime.of(2025, 1, 6, 10, 0, 0, 0, zoneId)
        val reminderTime = LocalTime.of(17, 3) // 17:03

        val result = ReminderUtils.calculateNextTrigger(reminderTime, everyDay, now)

        assertEquals(2025, result?.year)
        assertEquals(1, result?.monthValue)
        assertEquals(6, result?.dayOfMonth)
        assertEquals(17, result?.hour)
        assertEquals(3, result?.minute)
    }

    @Test
    fun `calculateNextTrigger - when scheduled time has passed today - returns tomorrow`() {
        // Monday 18:00
        val now = ZonedDateTime.of(2025, 1, 6, 18, 0, 0, 0, zoneId)
        val reminderTime = LocalTime.of(17, 3)

        val result = ReminderUtils.calculateNextTrigger(reminderTime, everyDay, now)

        assertEquals(2025, result?.year)
        assertEquals(1, result?.monthValue)
        assertEquals(7, result?.dayOfMonth) // Tuesday
        assertEquals(17, result?.hour)
    }

    @Test
    fun `calculateNextTrigger - when only weekends selected and today is Monday - returns Saturday`() {
        // Monday 10:00
        val now = ZonedDateTime.of(2025, 1, 6, 10, 0, 0, 0, zoneId)
        val reminderTime = LocalTime.of(10, 0)

        val result = ReminderUtils.calculateNextTrigger(reminderTime, weekends, now)

        assertEquals(2025, result?.year)
        assertEquals(1, result?.monthValue)
        assertEquals(11, result?.dayOfMonth) // Jan 11 is Saturday
    }

    @Test
    fun `calculateNextTrigger - when no days selected - returns null`() {
        val now = ZonedDateTime.now(zoneId)
        val result = ReminderUtils.calculateNextTrigger(LocalTime.of(9, 0), emptyList(), now)
        assertNull(result)
    }

    @Test
    fun `calculateNextTrigger - when only Monday selected and it is Monday evening - returns next Monday`() {
        // Monday Jan 6, 2025, 20:00
        val now = ZonedDateTime.of(2025, 1, 6, 20, 0, 0, 0, zoneId)
        val onlyMonday = listOf(true, false, false, false, false, false, false)
        val reminderTime = LocalTime.of(9, 0)

        val result = ReminderUtils.calculateNextTrigger(reminderTime, onlyMonday, now)

        assertEquals(2025, result?.year)
        assertEquals(1, result?.monthValue)
        assertEquals(13, result?.dayOfMonth) // Next Monday
    }
}
