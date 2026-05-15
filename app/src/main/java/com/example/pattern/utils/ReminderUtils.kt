package com.example.pattern.utils

import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object ReminderUtils {
    /**
     * Calculates the next trigger time for a reminder.
     * 
     * @param time The scheduled time (HH:mm).
     * @param selectedDays List of booleans representing Mon-Sun.
     * @param now The reference time (usually ZonedDateTime.now()).
     * @return The next ZonedDateTime the reminder should fire, or null if no days selected.
     */
    fun calculateNextTrigger(
        time: LocalTime,
        selectedDays: List<Boolean>,
        now: ZonedDateTime
    ): ZonedDateTime? {
        if (selectedDays.isEmpty() || !selectedDays.contains(true)) return null

        val today = now.toLocalDate()
        
        // Check if today is an option
        val dayOfWeekIndex = today.dayOfWeek.value - 1 // 1 (Mon) -> 0, ..., 7 (Sun) -> 6
        if (selectedDays.getOrElse(dayOfWeekIndex) { false }) {
            val todayTrigger = ZonedDateTime.of(today, time, now.zone)
            if (todayTrigger.isAfter(now)) {
                return todayTrigger
            }
        }

        // Find next day in the next 7 days
        for (i in 1..7) {
            val nextDay = today.plusDays(i.toLong())
            val nextDayOfWeekIndex = nextDay.dayOfWeek.value - 1
            if (selectedDays.getOrElse(nextDayOfWeekIndex) { false }) {
                return ZonedDateTime.of(nextDay, time, now.zone)
            }
        }

        return null
    }
}
