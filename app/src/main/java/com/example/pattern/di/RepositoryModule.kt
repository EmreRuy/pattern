package com.example.pattern.di

import com.example.pattern.data.repository.HabitRepositoryImpl
import com.example.pattern.domain.repository.HabitRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHabitRepository(
        habitRepositoryImpl: HabitRepositoryImpl
    ): HabitRepository
}
