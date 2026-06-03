package com.example.pattern.domain.usecase

import com.example.pattern.domain.repository.HabitRepository
import com.example.pattern.domain.util.DataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Staff-Level Business Logic:
 * Centralizes the freemium habit limit rule with DataResult integration.
 */
class CheckHabitLimitUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val isPremiumUserUseCase: IsPremiumUserUseCase
) {
    operator fun invoke(): Flow<HabitLimitStatus> = combine(
        habitRepository.getAllHabitsStream(),
        isPremiumUserUseCase()
    ) { habitsRes, isPremium ->
        if (habitsRes is DataResult.Loading) return@combine HabitLimitStatus.Loading
        if (habitsRes is DataResult.Error) return@combine HabitLimitStatus.Error
        
        val habitCount = (habitsRes as DataResult.Success).data.size
        
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
    data object Loading : HabitLimitStatus
    data object Error : HabitLimitStatus
    data object Unlimited : HabitLimitStatus
    data class Allowed(val currentCount: Int, val limit: Int) : HabitLimitStatus
    data class Reached(val limit: Int) : HabitLimitStatus
    
    val isLimitReached: Boolean get() = this is Reached
}
