package com.example.pattern.domain.model

data class HabitEmoji(
    val value: String,
    val category: String,
    val keywords: List<String> = emptyList()
)
