package com.deepseek.monitor.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun UsageRow(
    model: UsageModel,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hitRatio = TokenFormatter.cacheHitRatio(model.cacheHitTokens, model.cacheMissTokens)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // 上：模型名
        Text(
            model.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 下：左右分栏
        Row(modifier = Modifier.fillMaxWidth()) {
            // 左下：用量在上 + 缓存命中率在下，靠右对齐
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    TokenFormatter.fmtTokensShort(model.totalTokens) + " T",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    "缓存命中率 ${TokenFormatter.fmtPercent(hitRatio)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = LightColors.success,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 右下：金额
            Text(
                TokenFormatter.fmtMoney(model.cost),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun UsageSection(
    models: List<UsageModel>,
    onModelClick: (String) -> Unit,
    vertical: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (vertical) {
        Column(modifier = modifier.fillMaxWidth()) {
            models.forEach { model ->
                UsageRow(model = model, onClick = { onModelClick(model.key) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
        ) {
            models.forEach { model ->
                UsageRow(model = model, onClick = { onModelClick(model.key) }, modifier = Modifier.weight(1f))
            }
        }
    }
}
