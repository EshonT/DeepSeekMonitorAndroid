package com.deepseek.monitor.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deepseek.monitor.domain.model.UsageDay
import com.deepseek.monitor.presentation.theme.LightColors
import com.deepseek.monitor.util.TokenFormatter

@Composable
fun UsageTrendChart(
    days: List<UsageDay>,
    modifier: Modifier = Modifier
) {
    if (days.isEmpty()) return
    val chartDays = days

    val rawMax = chartDays.maxOf { it.totalTokens }.toFloat().coerceAtLeast(1f)
    val topValue = roundUpToNice(rawMax)
    val topLabel = TokenFormatter.fmtTokensShort(topValue.toLong())
    val axisColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    var tooltipDay by remember { mutableStateOf<UsageDay?>(null) }
    val chartH = 140.dp

    Column(modifier = modifier.fillMaxWidth()) {
        // 标题 + 图例同行
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "近7天用量",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            LegendItem(color = LightColors.chartHit, label = "命中")
            Spacer(modifier = Modifier.width(8.dp))
            LegendItem(color = LightColors.chartMiss, label = "未命中")
            Spacer(modifier = Modifier.width(8.dp))
            LegendItem(color = LightColors.chartResponse, label = "输出")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 图表：Row[ 纵轴 | 柱子区 ]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(start = 0.dp, end = 8.dp, top = 12.dp, bottom = 4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // 纵轴占位（不显示数字，顶部数值标在横线右侧）
                Spacer(modifier = Modifier.width(8.dp))

                // 柱子 + 日期
                Column(modifier = Modifier.weight(1f)) {
                    // 柱子行
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(chartH),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        chartDays.forEach { day ->
                            BarColumn(
                                day = day,
                                topValue = topValue,
                                onLongPress = { },
                                onTap = {
                                    tooltipDay = if (tooltipDay == day) null else day
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 日期标签
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        chartDays.forEach { day ->
                            Text(
                                day.date.takeLast(5), fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(44.dp)
                            )
                        }
                    }
                }
            }

            // 横轴线 + 顶部数值
            val axisModifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
                .height(chartH)
            Box {
                Canvas(modifier = Modifier.fillMaxWidth().height(chartH)) {
                    val h = size.height; val w = size.width
                    drawLine(axisColor, Offset(0f, 0f), Offset(w, 0f), 1.5.dp.toPx())     // 顶部
                    drawLine(axisColor, Offset(0f, h), Offset(w, h), 1.5.dp.toPx())         // 底部
                }
                // 顶部数值靠右上方
                Text(
                    topLabel,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.align(Alignment.TopEnd).offset(y = (-8).dp)
                )
            }

            // Tooltip
            val day = tooltipDay
            if (day != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-8).dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(day.date, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        val hitTotal = day.flashCacheHit + day.proCacheHit
                        val missTotal = day.flashCacheMiss + day.proCacheMiss
                        val respTotal = day.flashResponse + day.proResponse
                        Text("命中  ${TokenFormatter.fmtTokensShort(hitTotal)} tokens", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                        Text("未命中 ${TokenFormatter.fmtTokensShort(missTotal)} tokens", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                        Text("输出  ${TokenFormatter.fmtTokensShort(respTotal)} tokens", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BarColumn(
    day: UsageDay, topValue: Float,
    onLongPress: () -> Unit, onTap: () -> Unit
) {
    val barWidth = 40.dp
    val hitTokens = day.flashCacheHit + day.proCacheHit
    val missTokens = day.flashCacheMiss + day.proCacheMiss
    val respTokens = day.flashResponse + day.proResponse
    val total = hitTokens + missTokens + respTokens

    // 柱高按 topValue 比例
    val barH = if (total > 0 && topValue > 0f) {
        ((total.toFloat() / topValue) * 120f).dp.coerceAtLeast(2.dp)
    } else 2.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(44.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Canvas(
            modifier = Modifier
                .width(barWidth)
                .height(barH)
                .pointerInput(day.date) {
                    detectTapGestures(onLongPress = { onLongPress() }, onTap = { onTap() })
                }
        ) {
            val h = size.height; val w = size.width
            if (total == 0L) return@Canvas

            val respFrac = respTokens.toFloat() / total
            val missFrac = missTokens.toFloat() / total
            val hitFrac = hitTokens.toFloat() / total
            var top = h

            val respH = h * respFrac
            if (respH > 0f) { top -= respH; drawRoundRect(LightColors.chartResponse, Offset(0f, top), Size(w, respH), CornerRadius(0f)) }
            val missH = h * missFrac
            if (missH > 0f) { top -= missH; drawRoundRect(LightColors.chartMiss, Offset(0f, top), Size(w, missH), CornerRadius(0f)) }
            val hitH = h * hitFrac
            if (hitH > 0f) { top -= hitH; drawRoundRect(LightColors.chartHit, Offset(0f, top), Size(w, hitH), CornerRadius(4.dp.toPx())) }
        }
    }
}

private fun roundUpToNice(value: Float): Float {
    val m = value / 1_000_000f
    val nice = when {
        m <= 0 -> 1f
        m <= 1f -> kotlin.math.ceil(m * 10f) / 10f
        m <= 10f -> kotlin.math.ceil(m)
        m <= 100f -> kotlin.math.ceil(m / 10f) * 10f
        else -> kotlin.math.ceil(m / 50f) * 50f
    }
    return nice * 1_000_000f
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(8.dp)) { drawCircle(color) }
        Spacer(modifier = Modifier.width(3.dp))
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}
