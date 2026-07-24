package com.deepseek.monitor.di

import android.content.Context
import com.deepseek.monitor.data.local.datastore.ConfigDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 应用级 Hilt 模块。
 * 提供 DataStore、Room 等需要 Application Context 的单例依赖。
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideConfigDataStore(
        @ApplicationContext context: Context
    ): ConfigDataStore {
        return ConfigDataStore(context)
    }
}
