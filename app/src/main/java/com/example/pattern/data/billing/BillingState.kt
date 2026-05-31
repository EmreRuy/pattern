package com.example.pattern.data.billing

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

/**
 * Staff-Level implementation: Use sealed classes for explicit state management.
 * This prevents illegal UI states and simplifies debugging.
 */
sealed interface BillingState {
    data object Initializing : BillingState
    data object Loading : BillingState
    data object Disconnected : BillingState
    
    data class Success(
        val isPremium: Boolean,
        val activePurchases: List<Purchase> = emptyList(),
        val productDetails: Map<String, ProductDetails> = emptyMap()
    ) : BillingState

    data class Error(val message: String, val code: Int? = null) : BillingState
}
