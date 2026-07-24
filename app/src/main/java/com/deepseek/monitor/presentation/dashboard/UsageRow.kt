package com.deepseek.monitor.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deepseek.monitor.domain.model.UsageModel
import com.deepseek.monitor.presentation.theme.LightColors
import com.deepseek.monitor.util.TokenFormatter

/**
 * 单模型用量行组件。
 * 与 Windows 版仪表盘 UsageRow 布局一致：
 * 左侧模型名 + Token 进度条 + 费用，右侧缓存命中率。
 */
@Composable
fun UsageRow(
    model: UsageModel,
    maxTokens: Long = 10_000_000L,  // 进度条上限
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        // 第一行：模型名 + 费用
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (model.key) {
                "flash" -> "⚡"
                "pro" -> "🧠"
                else -> "📊"
            }
            Text(
                text = "$icon ${model.name}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = TokenFormatter.fmtMoney(model.cost),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = TokenFormatter.fmtTokensShort(model.totalTokens) + " T",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 进度条
        val progress = (model.totalTokens.toFloat() / maxTokens.coerceAtLeast(1)).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (model.key == "flash") LightColors.flash else LightColors.pro,
            trackColor = MaterialTheme.colorScheme.background,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 缓存命中率
        val hitRatio = TokenFormatter.cacheHitRatio(model.cacheHitTokens, model.cacheMissTokens)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "请求 ${TokenFormatter.fmtInt(model.requestCount)} 次",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "缓存命中 ${TokenFormatter.fmtPercent(hitRatio)}",
                style = MaterialTheme.typography.bodySmall,
                color = LightColors.success,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
