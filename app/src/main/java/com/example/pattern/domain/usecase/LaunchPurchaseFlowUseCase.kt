package com.example.pattern.domain.usecase

import android.app.Activity
import com.example.pattern.data.repository.PremiumRepository
import javax.inject.Inject

class LaunchPurchaseFlowUseCase @Inject constructor(
    private val repository: PremiumRepository
) {
    operator fun invoke(activity: Activity, productId: String) {
        repository.launchPurchaseFlow(activity, productId)
    }
}
