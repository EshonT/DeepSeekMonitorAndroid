package com.deepseek.monitor.presentation.daily

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deepseek.monitor.domain.model.UsageDay
import com.deepseek.monitor.presentation.theme.EInkColors
import com.deepseek.monitor.presentation.theme.LightColors
import com.deepseek.monitor.presentation.theme.LocalEInkMode
import com.deepseek.monitor.util.TokenFormatter

/**
 * 水平比例条：Flash vs Pro 当日 Token 总用量占比。
 *
 * 单条横向色块，左侧 Flash（蓝）、右侧 Pro（紫），
 * 宽度按各自 totalTokens 占比分配。
 */
@Composable
fun TokenProportionBar(
    today: UsageDay,
    modifier: Modifier = Modifier
) {
    val isEInk = LocalEInkMode.current
    val flashColor = if (isEInk) EInkColors.black else LightColors.flash
    val proColor = if (isEInk) EInkColors.darkGray else LightColors.pro
    val textColor = if (isEInk) EInkColors.black else MaterialTheme.colorScheme.onSurface
    val subColor = if (isEInk) EInkColors.darkGray else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    val flashTokens = today.flashTokens
    val proTokens = today.proTokens
    val total = (flashTokens + proTokens).coerceAtLeast(1)
    val flashRatio = flashTokens.toFloat() / total
    val proRatio = proTokens.toFloat() / total

    val flashPct = TokenFormatter.fmtPercent(flashRatio.toDouble())
    val proPct = TokenFormatter.fmtPercent(proRatio.toDouble())

    Column(modifier = modifier.fillMaxWidth()) {
        // 标题行
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Token 用量对比",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                TokenFormatter.fmtTokensShort(total) + " tokens",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 水平比例条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            if (flashRatio > 0f) {
                Box(
                    modifier = Modifier
                        .weight(flashRatio)
                        .height(32.dp)
                        .background(flashColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (flashRatio > 0.18f) {
                        Text(
                            "Flash $flashPct",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
            }
            if (proRatio > 0f) {
                Box(
                    modifier = Modifier
                        .weight(proRatio)
                        .height(32.dp)
                        .background(proColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (proRatio > 0.18f) {
                        Text(
                            "Pro $proPct",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 图例明细
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendDetail(
                color = flashColor,
                label = "V4 Flash",
                tokens = flashTokens,
                pct = flashPct,
                isEInk = isEInk
            )
            LegendDetail(
                color = proColor,
                label = "V4 Pro",
                tokens = proTokens,
                pct = proPct,
                isEInk = isEInk
            )
        }
    }
}

@Composable
private fun LegendDetail(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    tokens: Long,
    pct: String,
    isEInk: Boolean
) {
    val textColor = if (isEInk) EInkColors.black else MaterialTheme.colorScheme.onSurface
    val subColor = if (isEInk) EInkColors.darkGray else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color, radius = 6.dp.toPx())
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = textColor)
            Text(
                "${TokenFormatter.fmtTokensShort(tokens)} T · $pct",
                style = MaterialTheme.typography.labelMedium,
                color = subColor
            )
        }
    }
}

/**
 * 水平比例条：Flash vs Pro 当日费用占比。
 */
@Composable
fun CostProportionBar(
    today: UsageDay,
    modifier: Modifier = Modifier
) {
    val isEInk = LocalEInkMode.current
    val flashColor = if (isEInk) EInkColors.black else LightColors.flash
    val proColor = if (isEInk) EInkColors.darkGray else LightColors.pro
    val textColor = if (isEInk) EInkColors.black else MaterialTheme.colorScheme.onSurface
    val subColor = if (isEInk) EInkColors.darkGray else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    val flashCost = today.flashCost
    val proCost = today.proCost
    val total = (flashCost + proCost).coerceAtLeast(0.01)
    val flashRatio = (flashCost / total).toFloat().coerceIn(0f, 1f)
    val proRatio = (proCost / total).toFloat().coerceIn(0f, 1f)

    val flashPct = TokenFormatter.fmtPercent(flashRatio.toDouble())
    val proPct = TokenFormatter.fmtPercent(proRatio.toDouble())

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "今日费用",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                TokenFormatter.fmtMoney(total),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 26.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 水平比例条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            if (flashRatio > 0f) {
                Box(
                    modifier = Modifier
                        .weight(flashRatio.coerceAtLeast(0.03f))
                        .height(32.dp)
                        .background(flashColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (flashRatio > 0.18f) {
                        Text(
                            "Flash $flashPct",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
            }
            if (proRatio > 0f) {
                Box(
                    modifier = Modifier
                        .weight(proRatio.coerceAtLeast(0.03f))
                        .height(32.dp)
                        .background(proColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (proRatio > 0.18f) {
                        Text(
                            "Pro $proPct",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 图例行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CostLegend(color = flashColor, label = "V4 Flash", cost = flashCost, isEInk = isEInk)
            CostLegend(color = proColor, label = "V4 Pro", cost = proCost, isEInk = isEInk)
        }
    }
}

@Composable
private fun CostLegend(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    cost: Double,
    isEInk: Boolean
) {
    val textColor = if (isEInk) EInkColors.black else MaterialTheme.colorScheme.onSurface
    val subColor = if (isEInk) EInkColors.darkGray else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color, radius = 6.dp.toPx())
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            "$label  ${TokenFormatter.fmtMoney(cost)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}
