package com.example.pattern.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.resume
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow<BillingState>(BillingState.Initializing)
    val state = _state.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    // Configuration
    private val subProducts = listOf(
        QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT_MONTHLY)
            .setProductType(BillingClient.ProductType.SUBS)
            .build(),
        QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT_ANNUAL)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
    )

    private val inAppProducts = listOf(
        QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT_LIFETIME)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
    )

    init {
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    refresh()
                } else {
                    _state.value = BillingState.Error("Setup failed", billingResult.responseCode)
                }
            }

            override fun onBillingServiceDisconnected() {
                _state.value = BillingState.Disconnected
                scope.launch {
                    delay(RECONNECT_DELAY_MS)
                    startConnection()
                }
            }
        })
    }

    /**
     * Staff-Level: Atomic refresh of both purchases and product details (prices).
     * Now includes a callback for manual 'Restore' operations to provide UI feedback.
     */
    fun refresh(onComplete: (Boolean) -> Unit = {}) {
        if (!billingClient.isReady) {
            startConnection()
            onComplete(false)
            return
        }
        _state.value = BillingState.Loading

        scope.launch {
            try {
                val detailsResult = fetchProductDetails()
                val purchasesResult = fetchActivePurchases()
                
                val isPremium = purchasesResult.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                            purchase.products.intersect(PRODUCT_IDS).isNotEmpty()
                }

                _state.value = BillingState.Success(
                    isPremium = isPremium,
                    activePurchases = purchasesResult,
                    productDetails = detailsResult
                )
                onComplete(true)
            } catch (e: Exception) {
                _state.value = BillingState.Error(e.message ?: "Unknown error")
                onComplete(false)
            }
        }
    }

    private suspend fun fetchProductDetails(): Map<String, ProductDetails> = coroutineScope {
        val subs = async { queryProductDetailsSync(subProducts) }
        val inapp = async { queryProductDetailsSync(inAppProducts) }
        subs.await() + inapp.await()
    }

    private suspend fun queryProductDetailsSync(productList: List<QueryProductDetailsParams.Product>): Map<String, ProductDetails> = suspendCancellableCoroutine { cont ->
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        billingClient.queryProductDetailsAsync(params) { result, list ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                cont.resume(list.associateBy { it.productId })
            } else {
                cont.resume(emptyMap())
            }
        }
    }

    private suspend fun fetchActivePurchases(): List<Purchase> = coroutineScope {
        val subs = async { queryPurchasesSync(BillingClient.ProductType.SUBS) }
        val inapp = async { queryPurchasesSync(BillingClient.ProductType.INAPP) }
        subs.await() + inapp.await()
    }

    private suspend fun queryPurchasesSync(type: String): List<Purchase> = suspendCancellableCoroutine { cont ->
        val params = QueryPurchasesParams.newBuilder().setProductType(type).build()
        billingClient.queryPurchasesAsync(params) { result, list ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                cont.resume(list)
            } else {
                cont.resume(emptyList())
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { acknowledgeIfNecessary(it) }
                refresh()
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> { /* No-op */ }
            else -> {
                _state.value = BillingState.Error(billingResult.debugMessage, billingResult.responseCode)
            }
        }
    }

    private fun acknowledgeIfNecessary(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { /* Log result if needed */ }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productId: String) {
        val currentState = _state.value
        if (currentState !is BillingState.Success) return

        val productDetails = currentState.productDetails[productId] ?: return
        
        val offerToken = productDetails.subscriptionOfferDetails
            ?.maxByOrNull { it.pricingPhases.pricingPhaseList.size } // Pick best offer
            ?.offerToken ?: ""

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .apply { if (offerToken.isNotEmpty()) setOfferToken(offerToken) }
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    /**
     * Staff-Level: Utility to open the Google Play Subscription management page.
     */
    fun openSubscriptionManagement(activity: Activity) {
        val packageName = activity.packageName
        val url = "https://play.google.com/store/account/subscriptions?package=$packageName"
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse(url)
        }
        activity.startActivity(intent)
    }

    companion object {
        const val PRODUCT_MONTHLY = "premium_monthly"
        const val PRODUCT_ANNUAL = "premium_annual"
        const val PRODUCT_LIFETIME = "premium_lifetime"
        private val PRODUCT_IDS = setOf(PRODUCT_MONTHLY, PRODUCT_ANNUAL, PRODUCT_LIFETIME)
        private const val RECONNECT_DELAY_MS = 5000L
    }
}
