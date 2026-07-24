package com.deepseek.monitor.data.remote.interceptor

import com.deepseek.monitor.data.local.datastore.ConfigDataStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 认证拦截器。
 *
 * 支持两种 Token 注入模式：
 * - OFFICIAL:   使用 API Key（sk-xxx），注入到 api.deepseek.com 的请求
 * - PLATFORM:   使用 Web 登录 Token（JWT），注入到 platform.deepseek.com 的请求
 *
 * Token 来源均为 [ConfigDataStore] 的同步读取方法，
 * 避免在 OkHttp 拦截器线程中使用协程的线程切换开销。
 */
class AuthInterceptor @Inject constructor(
    private val configDataStore: ConfigDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val host = original.url.host

        // 根据请求域名选择对应 Token（同步读取，无协程开销）
        val token = when {
            host.contains("platform.deepseek.com") ->
                configDataStore.getUsageTokenSync()
            host.contains("api.deepseek.com") ->
                configDataStore.getApiKeySync()
            else -> null
        }

        val request = if (!token.isNullOrEmpty()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        return chain.proceed(request)
    }
}
