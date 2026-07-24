package com.deepseek.monitor.di

import com.deepseek.monitor.data.remote.api.DeepSeekApiService
import com.deepseek.monitor.data.remote.api.DeepSeekPlatformApiService
import com.deepseek.monitor.data.remote.interceptor.AuthInterceptor
import com.deepseek.monitor.data.remote.interceptor.ErrorInterceptor
import com.deepseek.monitor.data.remote.interceptor.LoggingInterceptorProvider
import com.deepseek.monitor.data.remote.interceptor.PlatformHeadersInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 网络层 Hilt 模块。
 *
 * 提供两个 Retrofit 实例：
 * - [DeepSeekApiService]：api.deepseek.com（余额查询）
 * - [DeepSeekPlatformApiService]：platform.deepseek.com（用量/费用查询）
 *
 * 两者共享同一个 OkHttpClient（含拦截器链），仅 Base URL 不同。
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_SECONDS = 15L

    /** 通用 OkHttpClient，含三个拦截器的链 */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        platformHeadersInterceptor: PlatformHeadersInterceptor,
        errorInterceptor: ErrorInterceptor,
        loggingProvider: LoggingInterceptorProvider,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            // 拦截器执行顺序：认证 → 平台请求头 → 错误处理 → 日志
            .addInterceptor(authInterceptor)
            .addInterceptor(platformHeadersInterceptor)
            .addInterceptor(errorInterceptor)
            .addInterceptor(loggingProvider.get())
            .build()
    }

    // ── 官方 API（api.deepseek.com） ──

    @Provides
    @Singleton
    fun provideDeepSeekApiService(okHttpClient: OkHttpClient): DeepSeekApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.deepseek.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeepSeekApiService::class.java)
    }

    // ── 平台 API（platform.deepseek.com） ──

    @Provides
    @Singleton
    fun provideDeepSeekPlatformApiService(okHttpClient: OkHttpClient): DeepSeekPlatformApiService {
        return Retrofit.Builder()
            .baseUrl("https://platform.deepseek.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeepSeekPlatformApiService::class.java)
    }
}
