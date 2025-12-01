package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pattern.ui.navigation.BottomNavigationItem
import com.example.pattern.ui.navigation.Screens

@Composable
fun CustomBottomBar(
    currentRoute: String,
    onItemClick: (BottomNavigationItem) -> Unit
) {
    val items = BottomNavigationItem().bottomNavigationItems()

    Box {
        BottomAppBar(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 6.dp
        ) {
            items.forEachIndexed { _, item ->
                if (item.route == Screens.Add.route) {
                    Spacer(modifier = Modifier.weight(0.5f))
                } else {
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (currentRoute == item.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 12.sp,
                                color = if (currentRoute == item.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        selected = currentRoute == item.route,
                        onClick = { onItemClick(item) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { onItemClick(items[1]) },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 5.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-24).dp)
                .size(64.dp)
        ) {
            Icon(imageVector = items[1].icon, contentDescription = items[1].label)
        }
    }
}

