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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pattern.R
import com.example.pattern.data.billing.BillingManager
import com.example.pattern.data.repository.PremiumStatus
import com.example.pattern.ui.screens.premiumScreen.PremiumViewModel
import kotlinx.coroutines.launch
import java.util.Locale

// Helper for formatting price
private fun Double.format(digits: Int) = "%.${digits}f".format(Locale.ENGLISH, this)

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

    // Dynamic Prices from Billing
    val lifetimePrice = (premiumStatus as? PremiumStatus.Loaded)
        ?.productDetails?.get(BillingManager.PRODUCT_LIFETIME)
        ?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$99.99"
    
    val annualDetails = (premiumStatus as? PremiumStatus.Loaded)
        ?.productDetails?.get(BillingManager.PRODUCT_ANNUAL)
        ?.subscriptionOfferDetails?.firstOrNull()
        ?.pricingPhases?.pricingPhaseList?.firstOrNull()
    
    val annualPrice = annualDetails?.formattedPrice ?: "$29.99"
    val annualMonthlyEquivalent = if (annualDetails != null) {
        val monthlyMicros = annualDetails.priceAmountMicros / 12
        "~${(monthlyMicros / 1000000.0).format(2)} ${annualDetails.priceCurrencyCode} / mo"
    } else "$2.49 / month"

    val monthlyPrice = (premiumStatus as? PremiumStatus.Loaded)
        ?.productDetails?.get(BillingManager.PRODUCT_MONTHLY)
        ?.subscriptionOfferDetails?.firstOrNull()
        ?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "$4.99"

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
                IconButton(
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
                shape = RoundedCornerShape(12.dp),
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
                text = "Level Up Your Habits",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Unlock advanced insights, unlimited habits, and premium customization.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // Pricing Tiers
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PremiumPlanCard(
                    title = "Lifetime",
                    subtitle = "One-time payment",
                    price = lifetimePrice,
                    isSelected = selectedPlan == 0,
                    onClick = { selectedPlan = 0 }
                )
                PremiumPlanCard(
                    title = "Annual",
                    subtitle = annualMonthlyEquivalent,
                    price = annualPrice,
                    tag = "BEST VALUE",
                    isSelected = selectedPlan == 1,
                    onClick = { selectedPlan = 1 }
                )
                PremiumPlanCard(
                    title = "Monthly",
                    subtitle = "Cancel anytime",
                    price = monthlyPrice,
                    isSelected = selectedPlan == 2,
                    onClick = { selectedPlan = 2 }
                )
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
                    "Unlimited Habit Patterns",
                    "Advanced Behavioral Analysis",
                    "Premium Color Palette",
                    "Cross-Device Sync (Coming Soon)",
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
            val ctaText = if (premiumStatus.isPremium) "Already Pro" 
                          else if (selectedPlan == 0) "Get Lifetime Access" 
                          else "Start Free Trial"

            Button(
                onClick = { 
                    if (premiumStatus.isPremium) {
                        viewModel.onManageSubscriptionClick(context as Activity)
                    } else {
                        viewModel.onPurchaseClick(context as Activity, selectedPlan)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (premiumStatus.isPremium) colorScheme.primary else colorScheme.onSurface
                )
            ) {
                Text(
                    text = ctaText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (premiumStatus.isPremium) colorScheme.onPrimary else colorScheme.surface
                )
            }

            Row(
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                        "Restore Purchase",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                if (premiumStatus.isPremium) {
                    TextButton(
                        onClick = { viewModel.onManageSubscriptionClick(context as Activity) }
                    ) {
                        Text(
                            "Manage Subscriptions",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.primary
                        )
                    }
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
private fun PremiumPlanCard(
    title: String,
    subtitle: String,
    price: String,
    tag: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val transition = updateTransition(isSelected, label = "SelectedState")
    
    val borderAlpha by transition.animateFloat(label = "BorderAlpha") { if (it) 1f else 0.1f }
    val backgroundColor by transition.animateColor(label = "BgColor") { if (it) colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent }

    Surface(
        onClick = onClick,
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
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
            Text(price, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
        }
    }
}
