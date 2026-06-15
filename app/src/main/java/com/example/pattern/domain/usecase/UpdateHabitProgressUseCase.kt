package com.example.pattern.domain.usecase

import com.example.pattern.domain.model.HabitDailyState
import com.example.pattern.domain.model.HabitType
import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.repository.UserRepository
import com.example.pattern.utils.ExperienceUtils
import java.time.LocalDate
import javax.inject.Inject

/**
 * Staff Engineer Refactoring:
 * Optimized timer transitions using a state-machine approach delegated to the Repository. 
 * This ensures architectural Single Source of Truth and atomic database consistency.
 * 
 * 1. Delegation: Complex state transitions are now handled atomically within the Repository/DAO.
 * 2. transactional Integrity: UseCase ensures that State changes and XP rewards are synced.
 * 3. Compose Stability: Domain models use ImmutableList and @Immutable for optimal rendering.
 */
class UpdateHabitProgressUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val userRepository: UserRepository
) {
    suspend fun startTimer(habitId: Int, date: LocalDate) {
        habitRepository.startTimer(habitId, date.toString(), System.currentTimeMillis())
    }

    suspend fun pauseTimer(habitId: Int, date: LocalDate) {
        habitRepository.pauseTimer(habitId, date.toString(), System.currentTimeMillis())
    }

    suspend fun resumeTimer(habitId: Int, date: LocalDate) {
        habitRepository.resumeTimer(habitId, date.toString(), System.currentTimeMillis())
    }

    suspend fun finishTimer(habitId: Int, date: LocalDate) {
        habitRepository.withTransaction {
            val habit = habitRepository.getHabitOnce(habitId) ?: return@withTransaction
            val updatedState = habitRepository.finishTimer(habitId, date.toString(), System.currentTimeMillis())
            
            if (updatedState != null) {
                userRepository.addXP(ExperienceUtils.calculateHabitXP(habit, updatedState))
            }
        }
    }

    suspend fun unfinishTimer(habitId: Int, date: LocalDate) {
        habitRepository.withTransaction {
            val dateStr = date.toString()
            val habit = habitRepository.getHabitOnce(habitId) ?: return@withTransaction
            val currentDaily = habitRepository.getDailyStateOnce(habitId, dateStr) ?: return@withTransaction
            if (!currentDaily.isCompleted) return@withTransaction
            
            val xpToSubtract = ExperienceUtils.calculateHabitXP(habit, currentDaily)
            
            // We reuse the basic upsert here for 'unfinish' as it's a simple state flip
            habitRepository.upsertDailyState(currentDaily.copy(isCompleted = false))
            userRepository.addXP(-xpToSubtract)
        }
    }

    suspend fun toggleTask(habitId: Int, date: LocalDate, completed: Boolean) {
        habitRepository.withTransaction {
            val dateStr = date.toString()
            val habit = habitRepository.getHabitOnce(habitId) ?: return@withTransaction
            
            // Get state before update to check if transition is needed
            val stateBefore = habitRepository.getDailyStateOnce(habitId, dateStr)
            val wasCompleted = when(habit.type) {
                HabitType.TASK, HabitType.QUIT -> stateBefore?.isTaskCompleted == true
                HabitType.BUILD -> stateBefore?.isCompleted == true
            }

            if (wasCompleted == completed) return@withTransaction

            // Atomic state change
            habitRepository.setTaskCompleted(habitId, dateStr, completed)
            
            // Fetch updated state for accurate XP calculation
            val updatedState = habitRepository.getDailyStateOnce(habitId, dateStr) ?: return@withTransaction
            
            val xpValue = if (completed) {
                ExperienceUtils.calculateHabitXP(habit, updatedState)
            } else {
                stateBefore?.let { -ExperienceUtils.calculateHabitXP(habit, it) } ?: 0
            }
            userRepository.addXP(xpValue)
        }
    }

    suspend fun incrementTask(habitId: Int, date: LocalDate) {
        habitRepository.withTransaction {
            val dateStr = date.toString()
            val habit = habitRepository.getHabitOnce(habitId) ?: return@withTransaction
            val stateBefore = habitRepository.getDailyStateOnce(habitId, dateStr) ?: HabitDailyState(habitId, dateStr)

            val taskCount = habit.taskCount ?: 1
            if (stateBefore.completedCount >= taskCount) return@withTransaction

            habitRepository.incrementTaskCount(habitId, dateStr)
            
            val stateAfter = habitRepository.getDailyStateOnce(habitId, dateStr)
            if (stateAfter?.isTaskCompleted == true && stateBefore.isTaskCompleted == false) {
                userRepository.addXP(ExperienceUtils.calculateHabitXP(habit, stateAfter))
            }
        }
    }
}
