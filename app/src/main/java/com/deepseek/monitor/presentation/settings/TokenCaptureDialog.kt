package com.deepseek.monitor.presentation.settings

import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.deepseek.monitor.data.local.webview.UsageTokenCaptureClient
import com.deepseek.monitor.presentation.theme.LightColors

/**
 * 全屏 WebView Dialog，用于登录 platform.deepseek.com 并自动捕获用量 Token。
 *
 * 捕获后 Token 自动填入设置页输入框，用户手动点击保存触发验证。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenCaptureDialog(
    onTokenCaptured: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var captured by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    val captureClient = remember {
        UsageTokenCaptureClient(
            onTokenCaptured = { token ->
                captured = true
                // 延迟关闭，让用户看到捕获成功的提示
                onTokenCaptured(token)
            },
            onPageFinished = { isLoading = false }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        if (captured) "Token 已捕获" else "登录 DeepSeek 平台",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, "关闭")
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )

            // 状态提示
            val statusText = when {
                isLoading -> "加载登录页面..."
                captured -> "Token 已自动填入，关闭此窗口后点击「保存 Token」按钮即可"
                else -> "请登录 DeepSeek 平台账号"
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(horizontal = 16.dp).let { it },
                        strokeWidth = 2.dp
                    )
                } else if (captured) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = LightColors.success,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 40.dp)
                )
            }

            // WebView
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                        captureClient.configure(this)
                        loadUrl("https://platform.deepseek.com")
                        webView = this
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
