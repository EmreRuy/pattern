package com.example.pattern.data.repository

import com.example.pattern.data.billing.BillingManager
import com.example.pattern.data.billing.BillingState
import com.example.pattern.data.local.dao.SettingsDao
import com.example.pattern.data.local.entity.SettingsEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumRepository @Inject constructor(
    private val billingManager: BillingManager,
    private val settingsDao: SettingsDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // High-level SSoT sync: Only sync when Billing state is authoritative (Success)
        scope.launch {
            billingManager.state
                .filterIsInstance<BillingState.Success>()
                .distinctUntilChanged { old, new -> old.isPremium == new.isPremium }
                .collectLatest { state ->
                    syncLocalCache(state.isPremium)
                }
        }
    }

    private suspend fun syncLocalCache(isPremium: Boolean) {
        val current = settingsDao.getSettingsOnce() ?: SettingsEntity()
        if (current.isPremium != isPremium) {
            settingsDao.upsertSettings(current.copy(isPremium = isPremium))
        }
    }

    /**
     * Rich state for the Premium/Paywall UI
     */
    val premiumStatus: Flow<PremiumStatus> = combine(
        settingsDao.getSettingsFlow().map { it?.isPremium ?: false },
        billingManager.state
    ) { localIsPremium, billingState ->
        when (billingState) {
            is BillingState.Success -> PremiumStatus.Loaded(
                isPremium = billingState.isPremium,
                productDetails = billingState.productDetails
            )
            is BillingState.Loading -> PremiumStatus.Loading(localIsPremium)
            is BillingState.Error -> PremiumStatus.Error(billingState.message, localIsPremium)
            else -> PremiumStatus.Loading(localIsPremium)
        }
    }.flowOn(Dispatchers.Default)

    fun launchPurchaseFlow(activity: android.app.Activity, productId: String) {
        billingManager.launchPurchaseFlow(activity, productId)
    }

    fun openSubscriptionManagement(activity: android.app.Activity) {
        billingManager.openSubscriptionManagement(activity)
    }

    /**
     * Staff-Level: Manual restoration of purchases with UI feedback.
     */
    fun restorePurchases(onComplete: (Boolean) -> Unit) {
        billingManager.refresh(onComplete)
    }

    /**
     * Staff-Level SSoT: Combines Local DB (instant) and Billing (authoritative).
     * This eliminates the "flicker" and ensures offline support.
     */
    fun isPremiumUser(): Flow<Boolean> = combine(
        settingsDao.getSettingsFlow().map { it?.isPremium ?: false }.distinctUntilChanged(),
        billingManager.state
    ) { local, billing ->
        when (billing) {
            is BillingState.Success -> billing.isPremium // Authoritative when available
            is BillingState.Error, BillingState.Disconnected -> local // Fallback to DB on network issues
            else -> local // Default to DB while Initializing/Loading
        }
    }.distinctUntilChanged()
    .flowOn(Dispatchers.Default)
}

sealed interface PremiumStatus {
    val isPremium: Boolean

    data class Loading(override val isPremium: Boolean) : PremiumStatus
    data class Loaded(override val isPremium: Boolean, val productDetails: Map<String, com.android.billingclient.api.ProductDetails>) : PremiumStatus
    data class Error(val message: String, override val isPremium: Boolean) : PremiumStatus
}
