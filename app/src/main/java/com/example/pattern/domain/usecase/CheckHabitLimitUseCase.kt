package com.example.pattern.domain.usecase

import com.example.pattern.data.repository.PremiumRepository
import com.example.pattern.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Staff-Level Business Logic:
 * Centralizes the freemium habit limit rule.
 * Returns true if the user is allowed to add a new habit.
 */
class CheckHabitLimitUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val premiumRepository: PremiumRepository
) {
    operator fun invoke(): Flow<HabitLimitStatus> = combine(
        habitRepository.getAllHabitsStream().map { it.size },
        premiumRepository.isPremiumUser()
    ) { habitCount, isPremium ->
        if (isPremium) {
            HabitLimitStatus.Unlimited
        } else {
            if (habitCount < FREE_HABIT_LIMIT) {
                HabitLimitStatus.Allowed(habitCount, FREE_HABIT_LIMIT)
            } else {
                HabitLimitStatus.Reached(FREE_HABIT_LIMIT)
            }
        }
    }

    companion object {
        const val FREE_HABIT_LIMIT = 5
    }
}

sealed interface HabitLimitStatus {
    data object Unlimited : HabitLimitStatus
    data class Allowed(val currentCount: Int, val limit: Int) : HabitLimitStatus
    data class Reached(val limit: Int) : HabitLimitStatus
    
    val isLimitReached: Boolean get() = this is Reached
}
