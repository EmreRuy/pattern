package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pattern.ui.navigation.BottomNavigationItem

@Composable
fun CustomBottomBar(
    navController: NavHostController,
    onItemClick: (BottomNavigationItem) -> Unit
) {
    val items = remember { BottomNavigationItem.items() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Senior Developer Optimization: 
    // We "sticky" the selection state to the last valid bottom bar route.
    // This prevents the icon from flickering to gray during transitions to detail screens.
    val selectedRoute by remember(currentDestination) {
        derivedStateOf {
            items.find { item -> 
                currentDestination?.hasRoute(item.route::class) == true 
            }?.route
        }
    }
    
    // Fallback to a persistent state to handle the "transitioning out" phase
    var lastValidRoute by remember { mutableStateOf<Any?>(null) }
    LaunchedEffect(selectedRoute) {
        if (selectedRoute != null) {
            lastValidRoute = selectedRoute
        }
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp, // Minimalistic look: no elevation shadow
        windowInsets = androidx.compose.material3.NavigationBarDefaults.windowInsets
    ) {
        items.forEach { item ->
            // Use the "sticky" route to keep the icon highlighted during detail transitions
            val isSelected = (selectedRoute ?: lastValidRoute)?.let { 
                it::class == item.route::class 
            } ?: false
            
            StandardNavigationBarItem(
                item = item,
                isSelected = isSelected,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
private fun RowScope.StandardNavigationBarItem(
    item: BottomNavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        animationSpec = tween(durationMillis = 300),
        label = "IconColor"
    )

    NavigationBarItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = iconColor
            )
        },
        label = {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            indicatorColor = Color.Transparent // Minimalistic: no pill indicator
        )
    )
}
