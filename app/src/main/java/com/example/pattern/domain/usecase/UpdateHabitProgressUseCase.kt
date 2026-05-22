package com.example.pattern.domain.usecase

import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.utils.ExperienceUtils
import java.time.LocalDate
import javax.inject.Inject

/**
 * Staff Engineer Refactoring:
 * Optimized timer transitions using the State Machine pattern. 
 * This reduces arithmetic complexity and improves database consistency.
 */
class UpdateHabitProgressUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend fun startTimer(habitId: Int, date: LocalDate) {
        val dateStr = date.toString()
        repository.getHabitOnce(habitId) ?: return
        val currentDaily = repository.getDailyStateOnce(habitId, dateStr)
        if (currentDaily?.isCompleted == true) return
        
        val updated = (currentDaily ?: HabitDailyState(habitId = habitId, date = dateStr)).copy(
            activeSessionStartMs = System.currentTimeMillis(),
            isCompleted = false
        )
        repository.upsertDailyState(updated)
    }

    suspend fun pauseTimer(habitId: Int, date: LocalDate) {
        val dateStr = date.toString()
        val currentDaily = repository.getDailyStateOnce(habitId, dateStr) ?: return
        
        // Safety check: can't pause if already completed or not running
        if (currentDaily.isCompleted || currentDaily.activeSessionStartMs == null) return
        
        val now = System.currentTimeMillis()
        val sessionDuration = (now - currentDaily.activeSessionStartMs).coerceAtLeast(0L)
        
        repository.upsertDailyState(currentDaily.copy(
            accumulatedTimeMs = currentDaily.accumulatedTimeMs + sessionDuration,
            activeSessionStartMs = null
        ))
    }

    suspend fun resumeTimer(habitId: Int, date: LocalDate) {
        val dateStr = date.toString()
        val currentDaily = repository.getDailyStateOnce(habitId, dateStr) ?: return
        
        // Safety check: can't resume if already completed or already running
        if (currentDaily.isCompleted || currentDaily.activeSessionStartMs != null) return
        
        repository.upsertDailyState(currentDaily.copy(
            activeSessionStartMs = System.currentTimeMillis()
        ))
    }

    suspend fun finishTimer(habitId: Int, date: LocalDate) {
        repository.withTransaction {
            val dateStr = date.toString()
            val habit = repository.getHabitOnce(habitId) ?: return@withTransaction
            val currentDaily = repository.getDailyStateOnce(habitId, dateStr) ?: HabitDailyState(habitId = habitId, date = dateStr)
            
            if (currentDaily.isCompleted) return@withTransaction
            
            val now = System.currentTimeMillis()
            val finalAccumulated = currentDaily.calculateTotalTimeMs(now)

            val updated = currentDaily.copy(
                isCompleted = true, 
                accumulatedTimeMs = finalAccumulated,
                activeSessionStartMs = null
            )
            repository.upsertDailyState(updated)
            repository.addXP(ExperienceUtils.calculateHabitXP(habit, updated))
        }
    }

    suspend fun unfinishTimer(habitId: Int, date: LocalDate) {
        repository.withTransaction {
            val dateStr = date.toString()
            val habit = repository.getHabitOnce(habitId) ?: return@withTransaction
            val currentDaily = repository.getDailyStateOnce(habitId, dateStr) ?: return@withTransaction
            if (!currentDaily.isCompleted) return@withTransaction
            
            repository.upsertDailyState(currentDaily.copy(isCompleted = false))
            repository.addXP(-ExperienceUtils.calculateHabitXP(habit, currentDaily))
        }
    }

    suspend fun toggleTask(habitId: Int, date: LocalDate, completed: Boolean) {
        repository.withTransaction {
            val dateStr = date.toString()
            val habit = repository.getHabitOnce(habitId) ?: return@withTransaction
            val currentDaily = repository.getDailyStateOnce(habitId, dateStr)
            
            val wasCompleted = when(habit.type) {
                HabitType.TASK, HabitType.QUIT -> currentDaily?.isTaskCompleted == true
                HabitType.BUILD -> currentDaily?.isCompleted == true
            }

            if (wasCompleted == completed) return@withTransaction

            val taskCount = habit.taskCount ?: 1
            val updatedState = (currentDaily ?: HabitDailyState(habitId = habitId, date = dateStr)).copy(
                isTaskCompleted = completed,
                isCompleted = completed,
                completedCount = if (completed) taskCount else 0
            )
            repository.upsertDailyState(updatedState)

            val xpChange = ExperienceUtils.calculateHabitXP(habit, updatedState)
            repository.addXP(if (completed) xpChange else -xpChange)
        }
    }

    suspend fun incrementTask(habitId: Int, date: LocalDate) {
        repository.withTransaction {
            val dateStr = date.toString()
            val habit = repository.getHabitOnce(habitId) ?: return@withTransaction
            val currentDaily = repository.getDailyStateOnce(habitId, dateStr) ?: HabitDailyState(habitId = habitId, date = dateStr)

            val taskCount = habit.taskCount ?: 1
            if (currentDaily.completedCount >= taskCount) return@withTransaction

            val newCount = currentDaily.completedCount + 1
            val isNewlyCompleted = newCount >= taskCount

            val updatedState = currentDaily.copy(
                completedCount = newCount,
                isTaskCompleted = isNewlyCompleted,
                isCompleted = isNewlyCompleted
            )
            repository.upsertDailyState(updatedState)

            if (isNewlyCompleted) {
                repository.addXP(ExperienceUtils.calculateHabitXP(habit, updatedState))
            }
        }
    }
}
