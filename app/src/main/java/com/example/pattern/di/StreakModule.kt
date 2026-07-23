package com.example.pattern.di

import com.example.pattern.domain.streak.StreakCalculator
import com.example.pattern.domain.streak.StreakCalculatorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StreakModule {

    @Binds
    @Singleton
    abstract fun bindStreakCalculator(
        streakCalculatorImpl: StreakCalculatorImpl
    ): StreakCalculator
}
