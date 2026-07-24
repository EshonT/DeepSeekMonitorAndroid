package com.deepseek.monitor.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

/**
 * HTTP 错误拦截器。
 *
 * 将 HTTP 错误码统一映射为可读的中文异常信息。
 * 与 Windows 版 lib.rs fetch_balance / fetch_usage 的错误处理保持一致。
 */
class ErrorInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (response.isSuccessful) return response

        val message = when (response.code) {
            401 -> "API Key 或 Token 无效或已过期，请重新配置"
            429 -> "请求过于频繁，请稍后再试"
            in 500..599 -> "DeepSeek 服务器错误: ${response.code}"
            else -> "请求失败: HTTP ${response.code}"
        }

        // 关闭响应体，避免资源泄漏
        response.close()

        throw IOException(message)
    }
}
