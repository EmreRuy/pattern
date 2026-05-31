package com.example.pattern.domain.usecase

import com.example.pattern.data.repository.PremiumRepository
import javax.inject.Inject

class RestorePurchasesUseCase @Inject constructor(
    private val repository: PremiumRepository
) {
    operator fun invoke(onComplete: (Boolean) -> Unit) {
        repository.restorePurchases(onComplete)
    }
}
