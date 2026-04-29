package com.example.pattern.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.R

@Composable
fun PremiumPlanScreen(
    onPurchase: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    // State to track selected plan
    var selectedPlan by remember { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(colorScheme.background)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        // 1. Header Section
        Icon(
            painter = painterResource(id = R.drawable.ic_crown),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Pattern Pro",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = colorScheme.onBackground
        )
        Text(
            text = "Master your discipline with total control.",
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        // 2. Multi-tier Plan Selection
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PlanCard(
                title = "Lifetime Access",
                subtitle = "Pay once, use forever",
                price = "$99.99",
                isSelected = selectedPlan == 0,
                onClick = { selectedPlan = 0 }
            )
            PlanCard(
                title = "Annual Membership",
                subtitle = "$2.49 / month",
                price = "$29.99",
                tag = "Best Value",
                isSelected = selectedPlan == 1,
                onClick = { selectedPlan = 1 }
            )
            PlanCard(
                title = "Monthly Access",
                subtitle = "Full access, billed monthly",
                price = "$4.99",
                isSelected = selectedPlan == 2,
                onClick = { selectedPlan = 2 }
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        // 3. Elegant Comparison Table
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)) {
                Text(
                    "FEATURES",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "FREE",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    "PRO",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.tertiary,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
            val features = listOf(
                "Unlimited Patterns" to false,
                "AI Pattern Analysis" to false,
                "Local-First Backup" to false,
                "Custom Themes" to false
            )
            features.forEach { (name, isFree) ->
                FeatureRow(name = name, isFree = isFree)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        // 4. High-Impact CTA
        Button(
            onClick = onPurchase,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.onBackground)
        ) {
            Text(
                text = if (selectedPlan == 0) "Unlock Everything Forever" else "Unlock",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Cancel anytime.",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun PlanCard(
    title: String,
    subtitle: String,
    price: String,
    tag: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val borderColor =
        if (isSelected) colorScheme.tertiary else colorScheme.outlineVariant.copy(alpha = 0.3f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        color = if (isSelected) colorScheme.tertiary.copy(alpha = 0.04f) else Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    if (tag != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = colorScheme.tertiary, shape = RoundedCornerShape(8.dp)) {
                            Text(
                                tag,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = colorScheme.onTertiary
                            )
                        }
                    }
                }
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Text(
                price,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
        }
    }
}

@Composable
fun FeatureRow(name: String, isFree: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        Icon(
            imageVector = if (isFree) Icons.Rounded.Check else Icons.Rounded.Close,
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .width(40.dp),
            tint = if (isFree) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else Color.Transparent
        )
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .width(40.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
    }
}