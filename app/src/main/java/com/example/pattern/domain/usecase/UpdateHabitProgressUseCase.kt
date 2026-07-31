package com.example.pattern.domain.usecase

import android.os.SystemClock
import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.repository.DailyLogRepository
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.utils.ExperienceUtils
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Staff Engineer Refactoring:
 * Optimized timer transitions using the State Machine pattern. 
 * 
 * Top 1% Refinements:
 * 1. Concurrency Safety: Uses a Mutex to prevent double-tap race conditions.
 * 2. Atomic XP: All operations are wrapped in DB transactions.
 * 3. Clock-Jump Protection: Uses SystemClock.elapsedRealtime() for live sessions.
 */
class UpdateHabitProgressUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val dailyLogRepository: DailyLogRepository
) {
    private val mutex = Mutex()
    
    /**
     * In-memory cache of session start times using elapsedRealtime().
     * This ensures timers are immune to system clock changes while the app is alive.
     */
    private val activeSessionElapsedStarts = ConcurrentHashMap<Int, Long>()

    suspend fun startTimer(habitId: Int, date: LocalDate) = mutex.withLock {
        habitRepository.withTransaction {
            val dateStr = date.toString()
            habitRepository.getHabitOnce(habitId) ?: return@withTransaction
            val currentDaily = dailyLogRepository.getDailyStateOnce(habitId, dateStr)
            
            // Safety: Don't start if already completed or already running
            if (currentDaily?.isCompleted == true || currentDaily?.activeSessionStartMs != null) return@withTransaction
            
            val updated = (currentDaily ?: HabitDailyState(habitId = habitId, date = dateStr)).copy(
                activeSessionStartMs = System.currentTimeMillis(),
                isCompleted = false
            )
            dailyLogRepository.upsertDailyState(updated)
            activeSessionElapsedStarts[habitId] = SystemClock.elapsedRealtime()
        }
    }

    suspend fun pauseTimer(habitId: Int, date: LocalDate) = mutex.withLock {
        habitRepository.withTransaction {
            val dateStr = date.toString()
            val currentDaily = dailyLogRepository.getDailyStateOnce(habitId, dateStr) ?: return@withTransaction
            
            // Safety check: can't pause if already completed or not running
            if (currentDaily.isCompleted || currentDaily.activeSessionStartMs == null) return@withTransaction
            
            val duration = calculateSessionDuration(habitId, currentDaily.activeSessionStartMs)
            
            dailyLogRepository.upsertDailyState(currentDaily.copy(
                accumulatedTimeMs = currentDaily.accumulatedTimeMs + duration,
                activeSessionStartMs = null
            ))
            activeSessionElapsedStarts.remove(habitId)
        }
    }

    suspend fun resumeTimer(habitId: Int, date: LocalDate) = mutex.withLock {
        habitRepository.withTransaction {
            val dateStr = date.toString()
            val currentDaily = dailyLogRepository.getDailyStateOnce(habitId, dateStr) ?: return@withTransaction
            
            // Safety check: can't resume if already completed or already running
            if (currentDaily.isCompleted || currentDaily.activeSessionStartMs != null) return@withTransaction
            
            dailyLogRepository.upsertDailyState(currentDaily.copy(
                activeSessionStartMs = System.currentTimeMillis()
            ))
            activeSessionElapsedStarts[habitId] = SystemClock.elapsedRealtime()
        }
    }

    suspend fun finishTimer(habitId: Int, date: LocalDate) = mutex.withLock {
        habitRepository.withTransaction {
            val dateStr = date.toString()
            val habit = habitRepository.getHabitOnce(habitId) ?: return@withTransaction
            val currentDaily = dailyLogRepository.getDailyStateOnce(habitId, dateStr) ?: HabitDailyState(habitId = habitId, date = dateStr)
            
            if (currentDaily.isCompleted) return@withTransaction
            
            val finalDuration = calculateSessionDuration(habitId, currentDaily.activeSessionStartMs)
            val finalAccumulated = currentDaily.accumulatedTimeMs + finalDuration

            val updated = currentDaily.copy(
                isCompleted = true, 
                accumulatedTimeMs = finalAccumulated,
                activeSessionStartMs = null
            )
            dailyLogRepository.upsertDailyState(updated)
            dailyLogRepository.addXP(ExperienceUtils.calculateHabitXP(habit, updated))
            activeSessionElapsedStarts.remove(habitId)
        }
    }

    suspend fun unfinishTimer(habitId: Int, date: LocalDate) = mutex.withLock {
        habitRepository.withTransaction {
            val dateStr = date.toString()
            val habit = habitRepository.getHabitOnce(habitId) ?: return@withTransaction
            val currentDaily = dailyLogRepository.getDailyStateOnce(habitId, dateStr) ?: return@withTransaction
            if (!currentDaily.isCompleted) return@withTransaction
            
            dailyLogRepository.upsertDailyState(currentDaily.copy(isCompleted = false))
            dailyLogRepository.addXP(-ExperienceUtils.calculateHabitXP(habit, currentDaily))
        }
    }

    suspend fun toggleTask(habitId: Int, date: LocalDate, completed: Boolean) = mutex.withLock {
        habitRepository.withTransaction {
            val dateStr = date.toString()
            val habit = habitRepository.getHabitOnce(habitId) ?: return@withTransaction
            val currentDaily = dailyLogRepository.getDailyStateOnce(habitId, dateStr)
            
            val wasCompleted = currentDaily?.isCompleted == true

            if (wasCompleted == completed) return@withTransaction

            val taskCount = habit.taskCount ?: 1
            val updatedState = (currentDaily ?: HabitDailyState(habitId = habitId, date = dateStr)).copy(
                isCompleted = completed,
                completedCount = if (completed) taskCount else 0
            )
            dailyLogRepository.upsertDailyState(updatedState)

            val xpChange = ExperienceUtils.calculateHabitXP(habit, updatedState)
            dailyLogRepository.addXP(if (completed) xpChange else -xpChange)
        }
    }

    suspend fun incrementTask(habitId: Int, date: LocalDate) = mutex.withLock {
        habitRepository.withTransaction {
            val dateStr = date.toString()
            val habit = habitRepository.getHabitOnce(habitId) ?: return@withTransaction
            val currentDaily = dailyLogRepository.getDailyStateOnce(habitId, dateStr) ?: HabitDailyState(habitId = habitId, date = dateStr)

            val taskCount = habit.taskCount ?: 1
            if (currentDaily.completedCount >= taskCount) return@withTransaction

            val newCount = currentDaily.completedCount + 1
            val isNewlyCompleted = newCount >= taskCount

            val updatedState = currentDaily.copy(
                completedCount = newCount,
                isCompleted = isNewlyCompleted
            )
            dailyLogRepository.upsertDailyState(updatedState)

            if (isNewlyCompleted) {
                dailyLogRepository.addXP(ExperienceUtils.calculateHabitXP(habit, updatedState))
            }
        }
    }

    /**
     * Anti-Cheat Duration Logic:
     * Calculates duration by preferring elapsedRealtime() to prevent clock jumps.
     * Falls back to System.currentTimeMillis() only if the session start is not in memory
     * (e.g. after an app restart).
     */
    private fun calculateSessionDuration(habitId: Int, persistedStartMs: Long?): Long {
        if (persistedStartMs == null) return 0L
        
        val elapsedStart = activeSessionElapsedStarts[habitId]
        val nowElapsed = SystemClock.elapsedRealtime()
        val nowWall = System.currentTimeMillis()
        
        return if (elapsedStart != null) {
            (nowElapsed - elapsedStart).coerceAtLeast(0L)
        } else {
            // Fallback for sessions resumed after app restart
            (nowWall - persistedStartMs).coerceAtLeast(0L)
        }
    }
}

