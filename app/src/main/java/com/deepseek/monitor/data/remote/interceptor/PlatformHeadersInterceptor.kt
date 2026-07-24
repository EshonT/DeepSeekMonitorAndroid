package com.deepseek.monitor.data.remote.interceptor

import com.deepseek.monitor.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 平台请求头拦截器。
 *
 * 为 platform.deepseek.com 的请求添加必要请求头，
 * 模拟官方客户端行为，避免被反爬/反滥用机制拦截。
 *
 * 参考官方 App 使用的:
 * - x-client-platform: android
 * - x-app-version: 当前应用版本号
 * - Accept: * / *
 * - User-Agent: 标准 Android WebView UA
 */
class PlatformHeadersInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // 仅对 platform.deepseek.com 添加平台请求头
        if (!original.url.host.contains("platform.deepseek.com")) {
            return chain.proceed(original)
        }

        val request = original.newBuilder()
            .header("x-client-platform", "android")
            .header("x-app-version", BuildConfig.VERSION_NAME)
            .header("Accept", "*/*")
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/148.0.0.0 Mobile Safari/537.36"
            )
            .build()

        return chain.proceed(request)
    }
}
