package com.deepseek.monitor.di

import com.deepseek.monitor.data.repository.BalanceRepositoryImpl
import com.deepseek.monitor.data.repository.ConfigRepositoryImpl
import com.deepseek.monitor.data.repository.UsageRepositoryImpl
import com.deepseek.monitor.domain.repository.BalanceRepository
import com.deepseek.monitor.domain.repository.ConfigRepository
import com.deepseek.monitor.domain.repository.UsageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository 绑定模块。
 * 将接口与实现的绑定关系集中于此。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBalanceRepository(impl: BalanceRepositoryImpl): BalanceRepository

    @Binds
    @Singleton
    abstract fun bindUsageRepository(impl: UsageRepositoryImpl): UsageRepository

    @Binds
    @Singleton
    abstract fun bindConfigRepository(impl: ConfigRepositoryImpl): ConfigRepository
}
