package com.example.pattern.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavigationItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Any
) {
    companion object {
        fun items(): List<BottomNavigationItem> = listOf(
            BottomNavigationItem(
                label = "Habits",
                selectedIcon = Icons.Rounded.GridView,
                unselectedIcon = Icons.Outlined.GridView,
                route = Destination.Home
            ),
            BottomNavigationItem(
                label = "Add",
                selectedIcon = Icons.Rounded.Add,
                unselectedIcon = Icons.Rounded.Add,
                route = Destination.Add
            ),
            BottomNavigationItem(
                label = "Insights",
                selectedIcon = Icons.Rounded.Analytics,
                unselectedIcon = Icons.Outlined.Analytics,
                route = Destination.Profile
            )
        )
    }
}
