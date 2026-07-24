package com.deepseek.monitor.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deepseek.monitor.presentation.theme.LightColors

/**
 * 阶段一占位页面。
 * 展示应用品牌和当前构建状态，后续由 DashboardScreen 替换。
 */
@Composable
fun PlaceholderScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 品牌图标占位
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(LightColors.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "DM",
                color = LightColors.onPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "DeepSeek Monitor",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Android v1.0.0",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 状态卡：展示阶段一完成状态
        StatusCard(
            title = "阶段一：项目骨架 ✅",
            items = listOf(
                "Clean Architecture 分层已就绪",
                "Retrofit 双实例 + 拦截器链",
                "Hilt DI 三模块注入",
                "三主题系统 (Light/Dark/EInk)",
                "EncryptedSharedPreferences 加密存储"
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatusCard(
            title = "阶段二：仪表盘 UI 🚧",
            items = listOf(
                "余额卡片 + 用量行",
                "7天趋势堆叠柱状图",
                "设置页 API Key 管理",
                "WebView Token 捕获"
            )
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 底部提示
        Text(
            text = "在 Android Studio 中打开本项目\n运行到模拟器或真机即可看到完整效果",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun StatusCard(title: String, items: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))
        items.forEach { item ->
            Row(
                modifier = Modifier.padding(vertical = 3.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightColors.primary,
                    modifier = Modifier.width(16.dp)
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
