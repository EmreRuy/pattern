package com.example.pattern.domain.usecase

import com.example.pattern.data.local.entity.HabitDailyState
import com.example.pattern.data.local.entity.HabitType
import com.example.pattern.data.repository.HabitRepository
import com.example.pattern.utils.ExperienceUtils
import java.time.LocalDate
import javax.inject.Inject

class UpdateHabitProgressUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend fun startTimer(habitId: Int, date: LocalDate) {
        val dateStr = date.toString()
        repository.getHabitOnce(habitId) ?: return
        val currentDaily = repository.getDailyStateOnce(habitId, dateStr)
        if (currentDaily?.isCompleted == true) return
        
        val updated = (currentDaily ?: HabitDailyState(habitId = habitId, date = dateStr)).copy(
            timerStartTime = System.currentTimeMillis(),
            timerPauseTime = null,
            isCompleted = false
        )
        repository.upsertDailyState(updated)
    }

    suspend fun pauseTimer(habitId: Int, date: LocalDate) {
        val dateStr = date.toString()
        val currentDaily = repository.getDailyStateOnce(habitId, dateStr) ?: return
        if (currentDaily.isCompleted || currentDaily.timerStartTime == null) return
        repository.upsertDailyState(currentDaily.copy(timerPauseTime = System.currentTimeMillis()))
    }

    suspend fun resumeTimer(habitId: Int, date: LocalDate) {
        val dateStr = date.toString()
        val currentDaily = repository.getDailyStateOnce(habitId, dateStr) ?: return
        if (currentDaily.isCompleted || currentDaily.timerStartTime == null || currentDaily.timerPauseTime == null) return
        val now = System.currentTimeMillis()
        val pausedDuration = now - currentDaily.timerPauseTime
        val newStartTime = currentDaily.timerStartTime + pausedDuration
        repository.upsertDailyState(currentDaily.copy(timerStartTime = newStartTime, timerPauseTime = null))
    }

    suspend fun finishTimer(habitId: Int, date: LocalDate) {
        val dateStr = date.toString()
        val habit = repository.getHabitOnce(habitId) ?: return
        val currentDaily = repository.getDailyStateOnce(habitId, dateStr) ?: HabitDailyState(habitId = habitId, date = dateStr)
        if (currentDaily.isCompleted) return
        
        val updated = currentDaily.copy(isCompleted = true, timerStartTime = null, timerPauseTime = null)
        repository.upsertDailyState(updated)
        repository.addXP(ExperienceUtils.calculateHabitXP(habit, updated))
    }

    suspend fun unfinishTimer(habitId: Int, date: LocalDate) {
        val dateStr = date.toString()
        val habit = repository.getHabitOnce(habitId) ?: return
        val currentDaily = repository.getDailyStateOnce(habitId, dateStr) ?: return
        if (!currentDaily.isCompleted) return
        
        repository.upsertDailyState(currentDaily.copy(isCompleted = false))
        repository.addXP(-ExperienceUtils.calculateHabitXP(habit, currentDaily))
    }

    suspend fun toggleTask(habitId: Int, date: LocalDate, completed: Boolean) {
        val dateStr = date.toString()
        val habit = repository.getHabitOnce(habitId) ?: return
        val currentDaily = repository.getDailyStateOnce(habitId, dateStr)
        
        val wasCompleted = when(habit.type) {
            HabitType.TASK, HabitType.QUIT -> currentDaily?.isTaskCompleted == true
            HabitType.BUILD -> currentDaily?.isCompleted == true
        }

        if (wasCompleted == completed) return

        val updatedState = (currentDaily ?: HabitDailyState(habitId = habitId, date = dateStr)).copy(
            isTaskCompleted = completed,
            isCompleted = completed 
        )
        repository.upsertDailyState(updatedState)

        val xpChange = ExperienceUtils.calculateHabitXP(habit, updatedState)
        repository.addXP(if (completed) xpChange else -xpChange)
    }
}
