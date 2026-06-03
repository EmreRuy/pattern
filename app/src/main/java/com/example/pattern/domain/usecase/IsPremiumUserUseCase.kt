package com.example.pattern.domain.usecase

import com.example.pattern.data.repository.PremiumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class IsPremiumUserUseCase @Inject constructor(
    private val repository: PremiumRepository
) {
    operator fun invoke(): Flow<Boolean> = flowOf(true) // repository.isPremiumUser()
}
