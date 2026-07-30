package com.deepseek.monitor.presentation.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deepseek.monitor.domain.model.UsageDay
import com.deepseek.monitor.presentation.theme.EInkColors
import com.deepseek.monitor.presentation.theme.LightColors
import com.deepseek.monitor.presentation.theme.LocalEInkMode
import com.deepseek.monitor.util.TokenFormatter
import androidx.compose.ui.platform.LocalDensity

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun UsageTrendChart(
    days: List<UsageDay>,
    modifier: Modifier = Modifier,
    fillHeight: Boolean = false
) {
    if (days.isEmpty()) return
    val chartDays = days

    val rawMax = chartDays.maxOf { it.totalTokens }.toFloat().coerceAtLeast(1f)
    val topValue = roundUpToNice(rawMax)
    val isEInk = LocalEInkMode.current
    val axisColor = if (isEInk) EInkColors.darkGray else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    val hitColor = if (isEInk) EInkColors.darkGray else LightColors.chartHit
    val missColor = if (isEInk) EInkColors.midGray else LightColors.chartMiss
    val respColor = if (isEInk) EInkColors.black else LightColors.chartResponse

    var tooltipDay by remember { mutableStateOf<UsageDay?>(null) }
    var tooltipIndex by remember { mutableStateOf(-1) }
    var tooltipWidthPx by remember { mutableIntStateOf(0) }

    Column(modifier = modifier
        .fillMaxWidth()
        .then(if (fillHeight) Modifier.fillMaxHeight() else Modifier)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("近7天用量", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.weight(1f))
            LegendItem(color = hitColor, label = "命中")
            Spacer(modifier = Modifier.width(8.dp))
            LegendItem(color = missColor, label = "未命中")
            Spacer(modifier = Modifier.width(8.dp))
            LegendItem(color = respColor, label = "输出")
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (fillHeight) Modifier.weight(1f) else Modifier.height(200.dp))
                .padding(start = 0.dp, end = 8.dp, top = 8.dp, bottom = 2.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val chartH = maxHeight

                // 图表区
                Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .drawBehind {
                                    drawLine(axisColor, Offset(0f, 0f), Offset(size.width, 0f), 2.dp.toPx())
                                    drawLine(axisColor, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx())
                                }
                                .pointerInput(chartDays.size) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown()
                                        down.consume()
                                        val barW = size.width.toFloat() / chartDays.size
                                        val idx = (down.position.x / barW).toInt().coerceIn(0, chartDays.size - 1)
                                        tooltipDay = chartDays[idx]
                                        tooltipIndex = idx
                                        var released = false
                                        while (!released) {
                                            val event = awaitPointerEvent()
                                            when (event.type) {
                                                PointerEventType.Move -> {
                                                    val pos = event.changes.firstOrNull()?.position ?: break
                                                    val i = (pos.x / barW).toInt().coerceIn(0, chartDays.size - 1)
                                                    tooltipDay = chartDays[i]
                                                tooltipIndex = i
                                                }
                                                PointerEventType.Release -> { tooltipDay = null; tooltipIndex = -1; released = true }
                                                else -> {}
                                            }
                                        }
                                    }
                                },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            chartDays.forEach { day ->
                                BarColumn(day = day, topValue = topValue, chartH = chartH,
                                    hitColor = hitColor, missColor = missColor, respColor = respColor)
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            chartDays.forEach { day ->
                                Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.Center) {
                                    Text(day.date.takeLast(5), fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }

                // Tooltip（自适应水平位置，跟随手指所在柱子）
                val day = tooltipDay
                if (day != null && tooltipIndex in chartDays.indices) {
                    val density = LocalDensity.current
                    val maxWidthPx = with(density) { maxWidth.toPx() }
                    val barWPx = maxWidthPx / chartDays.size
                    val tooltipYPx = with(density) { (-16).dp.roundToPx() }
                    Box(
                        modifier = Modifier.align(Alignment.TopStart).offset {
                            val centerX = (barWPx * (tooltipIndex + 0.5f)).toInt()
                            val x = (centerX - tooltipWidthPx / 2).coerceIn(0, (maxWidthPx - tooltipWidthPx).toInt())
                            IntOffset(x = x, y = tooltipYPx)
                        }
                            .onSizeChanged { tooltipWidthPx = it.width }
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
}

@Composable
private fun BarColumn(
    day: UsageDay, topValue: Float,
    chartH: androidx.compose.ui.unit.Dp,
    hitColor: Color, missColor: Color, respColor: Color
) {
    val barWidth = 40.dp
    val hitTokens = day.flashCacheHit + day.proCacheHit
    val missTokens = day.flashCacheMiss + day.proCacheMiss
    val respTokens = day.flashResponse + day.proResponse
    val total = hitTokens + missTokens + respTokens

    val maxBarH = chartH.value - 20f  // 预留日期标签空间
    val barH = if (total > 0 && topValue > 0f) {
        ((total.toFloat() / topValue) * maxBarH).dp.coerceAtLeast(2.dp)
    } else 2.dp

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(44.dp)) {
        Text(
            TokenFormatter.fmtTokensShort(total),
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(44.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Canvas(modifier = Modifier.width(barWidth).height(barH)) {
            val h = size.height; val w = size.width
            if (total == 0L) return@Canvas
            val respFrac = respTokens.toFloat() / total
            val missFrac = missTokens.toFloat() / total
            val hitFrac = hitTokens.toFloat() / total
            var top = h
            val respH = h * respFrac
            if (respH > 0f) { top -= respH; drawRoundRect(respColor, Offset(0f, top), Size(w, respH), CornerRadius(0f)) }
            val missH = h * missFrac
            if (missH > 0f) { top -= missH; drawRoundRect(missColor, Offset(0f, top), Size(w, missH), CornerRadius(0f)) }
            val hitH = h * hitFrac
            if (hitH > 0f) {
                top -= hitH
                val r = kotlin.math.min(4.dp.toPx(), hitH / 2)
                drawRoundRect(hitColor, Offset(0f, top - r), Size(w, hitH + r), CornerRadius(r, r))
                drawRect(hitColor, Offset(0f, top + r), Size(w, hitH - r))
            }
        }
    }
}

private fun roundUpToNice(value: Float): Float {
    val m = value / 1_000_000f
    val nice = when {
        m <= 0 -> 1f; m <= 1f -> kotlin.math.ceil(m * 10f) / 10f
        m <= 10f -> kotlin.math.ceil(m); m <= 100f -> kotlin.math.ceil(m / 10f) * 10f
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
