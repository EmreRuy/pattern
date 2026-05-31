package com.example.pattern.ui.screens.premiumScreen

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pattern.data.billing.BillingManager
import com.example.pattern.data.repository.PremiumRepository
import com.example.pattern.data.repository.PremiumStatus
import com.example.pattern.domain.usecase.IsPremiumUserUseCase
import com.example.pattern.domain.usecase.LaunchPurchaseFlowUseCase
import com.example.pattern.domain.usecase.RestorePurchasesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val premiumRepository: PremiumRepository,
    private val isPremiumUserUseCase: IsPremiumUserUseCase,
    private val launchPurchaseFlowUseCase: LaunchPurchaseFlowUseCase,
    private val restorePurchasesUseCase: RestorePurchasesUseCase
) : ViewModel() {

    // Simple boolean state for guards
    val isPremium = isPremiumUserUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Rich state for the paywall
    val premiumStatus: StateFlow<PremiumStatus> = premiumRepository.premiumStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PremiumStatus.Loading(false))

    fun onPurchaseClick(activity: Activity, planIndex: Int) {
        val productId = when (planIndex) {
            0 -> BillingManager.PRODUCT_LIFETIME
            1 -> BillingManager.PRODUCT_ANNUAL
            2 -> BillingManager.PRODUCT_MONTHLY
            else -> BillingManager.PRODUCT_MONTHLY
        }
        launchPurchaseFlowUseCase(activity, productId)
    }

    fun onRestoreClick(onComplete: (Boolean) -> Unit) {
        restorePurchasesUseCase(onComplete)
    }

    fun onManageSubscriptionClick(activity: Activity) {
        premiumRepository.openSubscriptionManagement(activity)
    }
}
