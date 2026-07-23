package com.example.pattern.domain.repository

import com.example.pattern.domain.model.HabitEmoji
import kotlinx.coroutines.flow.Flow

interface EmojiRepository {
    fun getAllEmojis(): Flow<List<HabitEmoji>>
    fun getCategories(): Flow<List<String>>
}
