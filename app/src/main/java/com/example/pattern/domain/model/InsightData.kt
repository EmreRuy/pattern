package com.example.pattern.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class InsightData(
    val title: String,
    val message: String,
    val action: String,
    val emoji: String
)
