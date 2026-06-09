package com.example.pattern.domain.repository

import com.example.pattern.domain.model.Settings
import com.example.pattern.domain.util.DataResult
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getTotalXPStream(): Flow<DataResult<Int>>
    fun getSettingsStream(): Flow<DataResult<Settings?>>
    suspend fun getSettingsOnce(): Settings?
    suspend fun updateQuietHours(enabled: Boolean, start: String, end: String)
    suspend fun addXP(xpToAdd: Int)
}
