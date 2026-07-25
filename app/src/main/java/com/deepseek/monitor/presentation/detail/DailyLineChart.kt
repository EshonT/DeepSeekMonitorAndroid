package com.deepseek.monitor.presentation.detail

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
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

/**
 * 折线图，纵轴两条水平线：底部 0 + 顶部自适应整十数，左侧标注。
 * 三条平滑曲线：命中、未命中、输出。
 */
@Composable
fun DailyLineChart(
    days: List<UsageDay>,
    model: String,
    modifier: Modifier = Modifier,
    fillHeight: Boolean = false
) {
    if (days.isEmpty()) return

    val points = days.mapIndexed { i, day ->
        val hit = if (model == "flash") day.flashCacheHit else day.proCacheHit
        val miss = if (model == "flash") day.flashCacheMiss else day.proCacheMiss
        val resp = if (model == "flash") day.flashResponse else day.proResponse
        PointData(i, hit, miss, resp, day.date.takeLast(5))
    }

    // 三条线的最大值（非堆积，各自独立）
    val rawMax = points.maxOf { maxOf(it.hit, it.miss, it.resp) }.toFloat()
    // 自适应整十数：向上取整到美观的刻度值
    val topValue = roundUpToNice(rawMax)
    val topLabel = TokenFormatter.fmtTokensShort(topValue.toLong())

    var tooltipIndex by remember { mutableStateOf(-1) }
    var tooltipWidthPx by remember { mutableIntStateOf(0) }
    val chartH = if (fillHeight) 130.dp else 170.dp
    val isEInk = LocalEInkMode.current
    val dotColor = MaterialTheme.colorScheme.onSurface
    val axisColor = if (isEInk) EInkColors.darkGray else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    val hitColor = if (isEInk) EInkColors.darkGray else LightColors.chartHit
    val missColor = if (isEInk) EInkColors.midGray else LightColors.chartMiss
    val respColor = if (isEInk) EInkColors.black else LightColors.chartResponse

    Column(modifier = modifier
        .fillMaxWidth()
        .then(if (fillHeight) Modifier.fillMaxHeight() else Modifier)) {
        // 图例
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Legend(color = hitColor, label = "命中")
            Spacer(modifier = Modifier.width(8.dp))
            Legend(color = missColor, label = "未命中")
            Spacer(modifier = Modifier.width(8.dp))
            Legend(color = respColor, label = "输出")
            Spacer(modifier = Modifier.width(8.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 图表主体
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (fillHeight) Modifier.weight(1f) else Modifier)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .padding(start = 0.dp, end = 8.dp, top = 8.dp, bottom = 2.dp)
        ) {
            // 纵轴顶部标注（浮层，不占宽度）
            Text(
                topLabel,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.TopStart).padding(start = 4.dp)
            )

            // 折线图画布
            Column(modifier = Modifier
                .fillMaxWidth()
                .then(if (fillHeight) Modifier.fillMaxHeight() else Modifier)) {
                Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (fillHeight) Modifier.weight(1f) else Modifier.height(chartH))
                            .pointerInput(points.size) {
                                awaitEachGesture {
                                    val down = awaitFirstDown()
                                    down.consume()
                                    val step = size.width / (points.size - 1).coerceAtLeast(1)
                                    tooltipIndex = ((down.position.x + step / 2) / step).toInt()
                                        .coerceIn(0, points.size - 1)

                                    var released = false
                                    while (!released) {
                                        val event = awaitPointerEvent()
                                        when (event.type) {
                                            PointerEventType.Move -> {
                                                val pos = event.changes.firstOrNull()?.position ?: break
                                                val step2 = size.width / (points.size - 1).coerceAtLeast(1)
                                                tooltipIndex = ((pos.x + step2 / 2) / step2).toInt()
                                                    .coerceIn(0, points.size - 1)
                                            }
                                            PointerEventType.Release -> {
                                                tooltipIndex = -1
                                                released = true
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                            }
                    ) {
                        val w = size.width
                        val h = size.height
                        val step = w / (points.size - 1).coerceAtLeast(1)

                        // Y 映射：topValue → y=0(顶部), 0 → y=h(底部)
                        fun y(value: Float): Float = if (topValue > 0f) h * (1f - value / topValue) else h

                        val respLine = points.map { Offset(it.index * step, y(it.resp.toFloat())) }
                        val missLine = points.map { Offset(it.index * step, y(it.miss.toFloat())) }
                        val hitLine = points.map { Offset(it.index * step, y(it.hit.toFloat())) }

                        // 顶部横线（topValue）
                        drawLine(axisColor, Offset(0f, 0f), Offset(w, 0f), 2.dp.toPx())

                        // 底部横线（0）
                        drawLine(axisColor, Offset(0f, h), Offset(w, h), 2.dp.toPx())

                        // 三条曲线
                        drawCurvePath(respLine, respColor)
                        drawCurvePath(missLine, missColor)
                        drawCurvePath(hitLine, hitColor)

                        // Tooltip 圆点
                        if (tooltipIndex in points.indices) {
                            val x = tooltipIndex * step
                            val pt = points[tooltipIndex]
                            val yy = y(maxOf(pt.hit, pt.miss, pt.resp).toFloat())
                            drawCircle(Color.White, 6.dp.toPx(), Offset(x, yy))
                            drawCircle(dotColor, 4.dp.toPx(), Offset(x, yy))
                        }
                    }

                    // X 轴日期
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        points.forEach { pt ->
                            Text(
                                pt.label, fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

            // Tooltip 浮层（自适应水平位置，跟随手指所在数据点）
            if (tooltipIndex in points.indices) {
                val pt = points[tooltipIndex]
                val density = LocalDensity.current
                val maxWidthPx = with(density) { maxWidth.toPx() }
                val stepPx = maxWidthPx / points.size.coerceAtLeast(2)
                val tooltipYPx = with(density) { (-16).dp.roundToPx() }
                Box(
                    modifier = Modifier.align(Alignment.TopStart).offset {
                        val centerX = (stepPx * (tooltipIndex + 0.5f)).toInt()
                        val x = (centerX - tooltipWidthPx / 2).coerceIn(0, (maxWidthPx - tooltipWidthPx).toInt())
                        IntOffset(x = x, y = tooltipYPx)
                    }
                        .onSizeChanged { tooltipWidthPx = it.width }
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(pt.label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "命中${TokenFormatter.fmtTokensShort(pt.hit)} · 未命中${TokenFormatter.fmtTokensShort(pt.miss)} · 输出${TokenFormatter.fmtTokensShort(pt.resp)}",
                            color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 向上取整到美观的整数值。
 * 例：138.87M → 140M, 5.34M → 6M, 0.46M → 0.5M
 */
private fun roundUpToNice(value: Float): Float {
    val m = value / 1_000_000f  // 转为 M 单位
    val nice = when {
        m <= 0 -> 1f
        m <= 1f -> (m * 10f).let { kotlin.math.ceil(it) } / 10f  // 0.1M 步进
        m <= 10f -> kotlin.math.ceil(m)                            // 1M 步进
        m <= 100f -> (kotlin.math.ceil(m / 10f) * 10f)             // 10M 步进
        else -> (kotlin.math.ceil(m / 50f) * 50f)                  // 50M 步进
    }
    return nice * 1_000_000f
}

private data class PointData(
    val index: Int, val hit: Long, val miss: Long, val resp: Long, val label: String
)

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCurvePath(
    points: List<Offset>, color: Color
) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            val prev = points[(i - 1).coerceAtLeast(0)]
            val curr = points[i]
            val next = points[(i + 1).coerceAtMost(points.size - 1)]
            cubicTo(
                prev.x + (curr.x - prev.x) * 0.5f, prev.y,
                curr.x - (next.x - curr.x) * 0.5f, curr.y,
                curr.x, curr.y
            )
        }
    }
    drawPath(path, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(8.dp)) { drawCircle(color) }
        Spacer(modifier = Modifier.width(3.dp))
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}