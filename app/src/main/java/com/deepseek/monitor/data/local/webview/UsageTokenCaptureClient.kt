package com.deepseek.monitor.data.local.webview

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * WebView Token 捕获客户端。
 *
 * 策略：通过 [WebViewClient.shouldInterceptRequest] 在原生层拦截
 * platform.deepseek.com 的 API 请求，从 Authorization 头提取 Bearer Token。
 *
 * 相比 JS 注入更可靠：不会误抓登录页面的临时 token，只拦截实际的业务 API 调用。
 */
class UsageTokenCaptureClient(
    private val onTokenCaptured: (String) -> Unit,
    private val onPageFinished: () -> Unit = {}
) {
    private var captured = false

    @SuppressLint("SetJavaScriptEnabled")
    fun configure(webView: WebView) {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                onPageFinished()
                // 页面加载后尝试从 localStorage 直接读取（备用通道）
                tryExtractFromLocalStorage(view)
            }

            // 主通道：原生层拦截 WebView 发出的 API 请求
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                extractFromRequest(request)
                return null  // 不阻止请求，仅拦截观察
            }
        }
    }

    // ── 通道一：shouldInterceptRequest 拦截 Authorization 头 ──

    private fun extractFromRequest(request: WebResourceRequest?) {
        if (captured || request == null) return

        val url = request.url.toString()
        // 仅拦截 platform.deepseek.com 的 API 请求
        if (!url.contains("platform.deepseek.com/api/")) return

        val headers = request.requestHeaders
        val auth = headers["Authorization"] ?: headers["authorization"] ?: return

        val token = extractBearer(auth)
        if (token != null) {
            deliver(token)
        }
    }

    // ── 通道二：localStorage 备用 ──

    private fun tryExtractFromLocalStorage(view: WebView?) {
        if (captured) return
        view?.evaluateJavascript(
            "(function(){ try { return localStorage.getItem('userToken') || ''; } catch(e) { return ''; } })()"
        ) { result ->
            val token = result?.trim('"', ' ', '\n')?.takeIf {
                it.isNotEmpty() && it != "null" && it.length >= 20
            }
            if (token != null) {
                deliver(token)
            }
        }
    }

    // ── 内部 ──

    private fun extractBearer(headerValue: String): String? {
        val regex = Regex("^Bearer\\s+(\\S+)", RegexOption.IGNORE_CASE)
        return regex.find(headerValue.trim())?.groupValues?.get(1)
    }

    private fun deliver(token: String) {
        if (captured) return
        captured = true
        onTokenCaptured(token)
    }
}
