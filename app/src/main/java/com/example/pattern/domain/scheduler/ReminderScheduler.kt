package com.example.pattern.domain.scheduler

import com.example.pattern.domain.model.Habit

interface ReminderScheduler {
    fun schedule(habit: Habit)
    fun cancel(habit: Habit)
}
