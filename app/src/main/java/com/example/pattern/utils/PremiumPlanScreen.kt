package com.example.pattern.utils

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pattern.data.billing.BillingManager
import com.example.pattern.data.repository.PremiumStatus
import com.example.pattern.ui.components.DebouncedIconButton
import com.example.pattern.ui.components.DebouncedIconButton
import com.example.pattern.ui.screens.premiumScreen.PremiumViewModel
import kotlinx.coroutines.launch

// Professional Pricing Implementation
@Composable
fun PremiumPlanScreen(
    onBack: () -> Unit = {},
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val premiumStatus by viewModel.premiumStatus.collectAsStateWithLifecycle()
    var selectedPlan by remember { mutableIntStateOf(1) }

    // Professional Price Resolution
    val pricingTiers = remember(premiumStatus) {
        val status = premiumStatus as? PremiumStatus.Loaded
        val productDetails = status?.productDetails ?: emptyMap()

        val lifetime = productDetails[BillingManager.PRODUCT_LIFETIME]
        val annual = productDetails[BillingManager.PRODUCT_ANNUAL]
        val monthly = productDetails[BillingManager.PRODUCT_MONTHLY]

        listOf(
            PricingTier(
                id = 0,
                title = "Lifetime",
                subtitle = "Unlimited access forever",
                price = lifetime?.oneTimePurchaseOfferDetails?.formattedPrice ?: "—",
                isLoading = premiumStatus is PremiumStatus.Loading
            ),
            PricingTier(
                id = 1,
                title = "Annual",
                subtitle = annual?.let { formatMonthlyEquivalent(it) } ?: "Best value",
                price = annual?.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice ?: "—",
                tag = "BEST VALUE",
                isLoading = premiumStatus is PremiumStatus.Loading
            ),
            PricingTier(
                id = 2,
                title = "Monthly",
                subtitle = "Flexible, cancel anytime",
                price = monthly?.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice ?: "—",
                isLoading = premiumStatus is PremiumStatus.Loading
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
    ) {
        // Aesthetic Background Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.primaryContainer.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.End) {
                DebouncedIconButton(
                    onClick = onBack,
                    modifier = Modifier.background(colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Brand Section
            Surface(
                color = colorScheme.primary,
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PATTERN PRO", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = colorScheme.onPrimary)
                }
            }

            Text(
                text = "Master Your Patterns",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Unlock advanced insights, unlimited habits, and full customization.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // Pricing Tiers
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                pricingTiers.forEach { tier ->
                    PremiumPlanCard(
                        title = tier.title,
                        subtitle = tier.subtitle,
                        price = tier.price,
                        tag = tier.tag,
                        isSelected = selectedPlan == tier.id,
                        isLoading = tier.isLoading,
                        onClick = { selectedPlan = tier.id }
                    )
                }
            }

            if (premiumStatus is PremiumStatus.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).clip(CircleShape),
                    color = colorScheme.primary,
                    trackColor = colorScheme.primaryContainer.copy(alpha = 0.2f)
                )
            } else {
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Feature List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                val features = listOf(
                    "Unlimited Habits",
                    "Advanced Behavioral Analysis",
                    "Premium Color Palette",
                    "Backup Habits",
                    "Priority Support"
                )
                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Check, 
                            contentDescription = null, 
                            tint = colorScheme.primary, 
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(feature, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // CTA
            val isPremium = premiumStatus.isPremium
            val ctaText = when {
                isPremium -> "Already Pro"
                selectedPlan == 0 -> "Unlock Lifetime"
                else -> "Start Your Journey"
            }

            Button(
                onClick = { 
                    if (isPremium) {
                        viewModel.onManageSubscriptionClick(context as Activity)
                    } else {
                        viewModel.onPurchaseClick(context as Activity, selectedPlan)
                    }
                },
                enabled = premiumStatus !is PremiumStatus.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPremium) colorScheme.primary else colorScheme.onSurface
                )
            ) {
                Text(
                    text = ctaText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isPremium) colorScheme.onPrimary else colorScheme.surface
                )
            }

            // Footer Links
            Row(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = {
                        viewModel.onRestoreClick { success ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (success) "Purchases restored successfully" 
                                    else "No active purchases found"
                                )
                            }
                        }
                    }
                ) {
                    Text(
                        "Restore",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                DotSeparator(colorScheme)

                TextButton(
                    onClick = { viewModel.onManageSubscriptionClick(context as Activity) }
                ) {
                    Text(
                        "Subscriptions",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                DotSeparator(colorScheme)

                TextButton(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            data = "https://www.google.com".toUri() // Replace with your actual policy URL
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        "Privacy Policy",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun DotSeparator(colorScheme: ColorScheme) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(3.dp)
            .background(colorScheme.onSurfaceVariant.copy(alpha = 0.2f), CircleShape)
    )
}

private data class PricingTier(
    val id: Int,
    val title: String,
    val subtitle: String,
    val price: String,
    val tag: String? = null,
    val isLoading: Boolean
)

private fun formatMonthlyEquivalent(productDetails: com.android.billingclient.api.ProductDetails): String {
    val phase = productDetails.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.lastOrNull() 
        ?: return "Best value"
    
    return try {
        val monthlyAmount = phase.priceAmountMicros / 12 / 1000000.0
        val format = java.text.NumberFormat.getCurrencyInstance().apply {
            currency = java.util.Currency.getInstance(phase.priceCurrencyCode)
            maximumFractionDigits = 2
        }
        "~${format.format(monthlyAmount)} / month"
    } catch (_: Exception) {
        "Best value"
    }
}

@Composable
private fun PremiumPlanCard(
    title: String,
    subtitle: String,
    price: String,
    tag: String? = null,
    isSelected: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val transition = updateTransition(isSelected, label = "SelectedState")
    
    val borderAlpha by transition.animateFloat(label = "BorderAlpha") { if (it) 1f else 0.1f }
    val backgroundColor by transition.animateColor(label = "BgColor") { if (it) colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent }

    Surface(
        onClick = if (isLoading) ({}) else onClick,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, colorScheme.primary.copy(alpha = borderAlpha)),
        color = backgroundColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    if (tag != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = colorScheme.primary, shape = RoundedCornerShape(8.dp)) {
                            Text(
                                tag,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                                color = colorScheme.onPrimary
                            )
                        }
                    }
                }
                Text(
                    text = subtitle, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.then(if (isLoading && subtitle == "—") Modifier.width(80.dp).background(colorScheme.surfaceVariant, CircleShape) else Modifier)
                )
            }
            if (isLoading && price == "—") {
                Box(modifier = Modifier.size(width = 60.dp, height = 24.dp).clip(CircleShape).background(colorScheme.surfaceVariant))
            } else {
                Text(price, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
            }
        }
    }
}
