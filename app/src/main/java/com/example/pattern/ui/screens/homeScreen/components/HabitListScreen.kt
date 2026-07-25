package com.example.pattern.ui.screens.homeScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pattern.ui.components.DebouncedIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    onHabitClick: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: HabitListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Remember stable callbacks to prevent unnecessary recompositions
    val stableOnBack = remember(onBack) { onBack }
    val stableOnHabitClick = remember(onHabitClick) { onHabitClick }

    var isSearchExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            HabitListTopBar(
                isSearchExpanded = isSearchExpanded,
                viewModel = viewModel, // Pass VM to scoped components
                onSearchExpandedChange = { isSearchExpanded = it },
                onBack = stableOnBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(padding)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                val state = uiState
                if (state.error != null) {
                    Text(
                        text = state.error,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    HabitListBody(
                        habits = state.habits,
                        isLoading = state.isLoading,
                        onHabitClick = stableOnHabitClick
                    )
                }
            }


            HabitSummaryHeader(summary = uiState.summary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitListTopBar(
    isSearchExpanded: Boolean,
    viewModel: HabitListViewModel,
    onSearchExpandedChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    if (isSearchExpanded) {
        // Scoped search query collection prevents whole screen recomposition
        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
        
        SearchTopAppBar(
            query = searchQuery,
            onQueryChange = viewModel::onSearchQueryChange,
            onSearchCollapsed = {
                onSearchExpandedChange(false)
                viewModel.onSearchQueryChange("")
            }
        )
    } else {
        DefaultTopAppBar(
            onBack = onBack,
            onSearchClick = { onSearchExpandedChange(true) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultTopAppBar(
    onBack: () -> Unit,
    onSearchClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                "ALL HABITS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            )
        },
        navigationIcon = {
            DebouncedIconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Rounded.Search, contentDescription = "Search")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchCollapsed: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search habits...") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true
            )
        },
        navigationIcon = {
            IconButton(onClick = onSearchCollapsed) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBackIos, contentDescription = "Back")
            }
        }
    )
}

@Composable
private fun HabitSummaryHeader(summary: com.example.pattern.domain.usecase.HabitSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            SummaryItem(label = "Total", value = summary.total.toString())
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            SummaryItem(label = "Done Today", value = summary.completed.toString())
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            SummaryItem(label = "XP Today", value = "+${summary.dailyXP}")
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
