package com.deepseek.monitor.data.remote.interceptor

import com.deepseek.monitor.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject

/**
 * Debug 日志拦截器工厂。
 *
 * Debug 构建：记录请求/响应头和 Body（BODY 级别）。
 * Release 构建：不记录。
 */
class LoggingInterceptorProvider @Inject constructor() {

    fun get(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
}
