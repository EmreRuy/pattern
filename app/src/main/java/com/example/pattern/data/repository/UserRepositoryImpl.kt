package com.example.pattern.data.repository

import androidx.room.withTransaction
import com.example.pattern.data.local.dao.SettingsDao
import com.example.pattern.data.local.db.AppDataBase
import com.example.pattern.data.local.entity.SettingsEntity
import com.example.pattern.data.mapper.toDomain
import com.example.pattern.domain.model.Settings
import com.example.pattern.domain.repository.UserRepository
import com.example.pattern.domain.util.DataResult
import com.example.pattern.domain.util.asResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val database: AppDataBase,
    private val settingsDao: SettingsDao
) : UserRepository {

    override fun getTotalXPStream(): Flow<DataResult<Int>> =
        settingsDao.getSettingsFlow()
            .map { it?.totalXP ?: 0 }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .asResult()

    override fun getSettingsStream(): Flow<DataResult<Settings?>> =
        settingsDao.getSettingsFlow()
            .distinctUntilChanged()
            .map { it?.toDomain() }
            .flowOn(Dispatchers.Default)
            .asResult()

    override suspend fun getSettingsOnce(): Settings? = 
        settingsDao.getSettingsOnce()?.toDomain()

    override suspend fun updateQuietHours(enabled: Boolean, start: String, end: String) {
        database.withTransaction {
            val current = settingsDao.getSettingsOnce() ?: SettingsEntity()
            settingsDao.upsertSettings(
                current.copy(
                    quietHoursEnabled = enabled,
                    startTime = start,
                    endTime = end
                )
            )
        }
    }

    override suspend fun addXP(xpToAdd: Int) {
        database.withTransaction {
            val current = settingsDao.getSettingsOnce() ?: SettingsEntity()
            settingsDao.upsertSettings(
                current.copy(totalXP = current.totalXP + xpToAdd)
            )
        }
    }
}
