package com.deepseek.monitor.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import android.content.res.Configuration
import com.deepseek.monitor.presentation.common.ErrorView
import com.deepseek.monitor.presentation.common.LoadingView
import com.deepseek.monitor.presentation.theme.LightColors
import androidx.compose.ui.platform.LocalConfiguration

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
    val chartFullRefreshKey = state.chartFullRefreshKey

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp)
    ) {
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
                val today = usageResult?.days?.find {
                    it.date == java.time.LocalDate.now().toString()
                }
                BalanceCard(
                    balance = balance,
                    todayUsage = today,
                    refreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    onSettings = onNavigateToSettings
                )
            }
        }

        // ── 模型用量 + 趋势图 ──
        when (usageState) {
            DataState.Loading -> LoadingView(
                message = "加载用量数据...",
                modifier = Modifier.weight(1f)
            )
            is DataState.Error -> ErrorView(
                message = usageState.message,
                onRetry = { viewModel.refresh() },
                modifier = Modifier.fillMaxWidth()
            )
            else -> {
                val usage = usageResult
                if (usage != null) {
                    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                    val today = java.time.LocalDate.now().toString()
                    val pastDays = usageResult.days
                        .filter { it.date <= today }
                        .takeLast(7)

                    if (isLandscape && pastDays.isNotEmpty()) {
                        // 横屏：左卡片 + 右图表（撑满高度）
                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            Column(modifier = Modifier.weight(0.3f)) {
                                UsageSection(models = usage.models, onModelClick = onNavigateToDetail, vertical = true)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            key(chartFullRefreshKey) { UsageTrendChart(days = pastDays, modifier = Modifier.weight(0.7f), fillHeight = true) }
                        }
                    } else {
                        // 竖屏：卡片 + 图表（自然高度）
                        UsageSection(models = usage.models, onModelClick = onNavigateToDetail, vertical = true)
                        if (pastDays.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            key(chartFullRefreshKey) { UsageTrendChart(days = pastDays) }
                        }
                    }
                }
            }
        }
    }
}

