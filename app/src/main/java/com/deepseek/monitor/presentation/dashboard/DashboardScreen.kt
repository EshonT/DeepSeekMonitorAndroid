package com.deepseek.monitor.presentation.dashboard

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.deepseek.monitor.presentation.common.ErrorView
import com.deepseek.monitor.presentation.common.LoadingView
import com.deepseek.monitor.presentation.theme.LightColors

/**
 * 仪表盘主页面。
 * 布局：品牌栏 + 余额卡片 + 模型用量行 + 7天趋势图。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // 提取本地变量，避免委托属性智能转换问题
    val balanceState = state.balanceState
    val usageState = state.usageState
    val balance = state.balance
    val usageResult = state.usageResult
    val isRefreshing = state.isRefreshing

    // 每次从其他页面返回时自动刷新数据
    val lifecycleOwner = LocalLifecycleOwner.current
    androidx.compose.runtime.LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refresh()
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            TopBar(onSettingsClick = onNavigateToSettings)

            Spacer(modifier = Modifier.height(16.dp))

            // ── 余额区域 ──
            when (balanceState) {
                DataState.Loading -> LoadingView(
                    message = "查询余额...",
                    modifier = Modifier.height(200.dp)
                )
                is DataState.Error -> ErrorView(
                    message = balanceState.message,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.fillMaxWidth()
                )
                else -> {
                    val b = balance
                    if (b != null) {
                        BalanceCard(balance = b)
                    } else {
                        NoApiKeyPlaceholder()
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 模型用量行 ──
            val usage = usageResult
            if (usage != null) {
                val maxToken = usage.models
                    .maxOfOrNull { it.totalTokens }?.let { it * 2 } ?: 10_000_000L

                usage.models.forEach { model ->
                    UsageRow(
                        model = model,
                        maxTokens = maxToken,
                        onClick = { onNavigateToDetail(model.key) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // ── 7天趋势图 ──
                if (usage.days.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ChartPlaceholder(dayCount = usage.days.size)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 未配置 API Key 时的空状态提示。
 */
@Composable
private fun NoApiKeyPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🔑", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "未配置 API Key",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "点击右上角设置图标添加",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * 顶部栏：品牌图标 + 标题 + 设置按钮。
 */
@Composable
private fun TopBar(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 品牌图标
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(LightColors.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "DM",
                color = LightColors.onPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "DeepSeek Monitor",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "设置",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 7天趋势图占位（阶段三替换为 Vico 堆叠柱状图）。
 */
@Composable
private fun ChartPlaceholder(dayCount: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = "缓存命中明细",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 简化的柱状图示意（纯 Compose Canvas）
        Row(
            modifier = Modifier.fillMaxWidth().height(80.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            repeat(dayCount) {
                val h1 = (30..70).random()
                val h2 = (15..40).random()
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(
                        modifier = Modifier.width(24.dp).height((h1 + h2).dp)
                    ) {
                        drawRect(
                            color = LightColors.chartHit,
                            size = size.copy(height = size.height * h1 / (h1 + h2))
                        )
                        drawRect(
                            color = LightColors.chartMiss,
                            size = size.copy(height = size.height * h2 / (h1 + h2)),
                            topLeft = Offset(0f, size.height * h1 / (h1 + h2))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendDot(color = LightColors.chartHit, label = "命中")
            Spacer(modifier = Modifier.width(16.dp))
            LegendDot(color = LightColors.chartMiss, label = "未命中")
            Spacer(modifier = Modifier.width(16.dp))
            LegendDot(color = LightColors.chartResponse, label = "输出")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
