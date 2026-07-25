package com.deepseek.monitor.presentation.detail

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepseek.monitor.domain.model.UsageModel
import com.deepseek.monitor.presentation.common.ErrorView
import com.deepseek.monitor.presentation.common.LoadingView
import com.deepseek.monitor.presentation.dashboard.DataState
import com.deepseek.monitor.presentation.theme.LightColors
import com.deepseek.monitor.util.TokenFormatter

@Composable
fun DetailScreen(
    model: String,
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val usageResult = state.usageResult
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val usageState = state.usageState
    when (usageState) {
        DataState.Loading -> LoadingView(message = "加载用量数据...")
        is DataState.Error -> ErrorView(
            message = usageState.message,
            onRetry = { viewModel.refresh(model) }
        )
        else -> {
            val usage = usageResult
            if (usage != null) {
                val modelData = usage.models.find { it.key == model }
                val name = "V4 ${if (model == "flash") "Flash" else "Pro"}"

                val todayStr = java.time.LocalDate.now().toString()
                val recentDays = usage.days
                    .filter { it.date <= todayStr }
                    .takeLast(7)

                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // 页首
                    HeaderRow(name, modelData, model, onNavigateBack)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLandscape) {
                        LandscapeLayout(modelData, recentDays, model)
                    } else {
                        PortraitLayout(modelData, recentDays, model)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderRow(name: String, modelData: UsageModel?, model: String, onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onNavigateBack() }
        )
        Spacer(modifier = Modifier.weight(1f))
        if (modelData != null) {
            Text(
                TokenFormatter.fmtMoney(modelData.cost),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** 竖屏：三卡片横排 + 图表 */
@Composable
private fun PortraitLayout(
    modelData: UsageModel?,
    recentDays: List<com.deepseek.monitor.domain.model.UsageDay>,
    model: String
) {
    if (modelData != null) {
        StatRow(modelData)
        Spacer(modifier = Modifier.height(12.dp))
    }
    Text(
        "按日 Token 消耗",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(4.dp))
    DailyLineChart(days = recentDays, model = model)
}

/** 横屏：左右 2:8 布局（左侧卡片竖排 + 右侧图表） */
@Composable
private fun LandscapeLayout(
    modelData: UsageModel?,
    recentDays: List<com.deepseek.monitor.domain.model.UsageDay>,
    model: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth().fillMaxHeight()) {
        // 左侧卡片区 15%
        Column(
            modifier = Modifier.fillMaxHeight().width((LocalConfiguration.current.screenWidthDp * 0.20f).dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            if (modelData != null) {
                Spacer(modifier = Modifier.height(4.dp))
                StatCardWide("请求次数", TokenFormatter.fmtInt(modelData.requestCount))
                StatCardWide("缓存命中", TokenFormatter.fmtTokensShort(modelData.cacheHitTokens))
                StatCardWide("缓存未命中", TokenFormatter.fmtTokensShort(modelData.cacheMissTokens))
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 右侧图表区
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Text(
                "按日 Token 消耗",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            DailyLineChart(days = recentDays, model = model, fillHeight = true, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatRow(modelData: UsageModel) {
    Box(modifier = Modifier.fillMaxWidth()) {
        StatCard("请求次数", TokenFormatter.fmtInt(modelData.requestCount),
            Modifier.align(Alignment.CenterStart).fillMaxWidth(0.3f)
        )
        StatCard("缓存命中", TokenFormatter.fmtTokensShort(modelData.cacheHitTokens),
            Modifier.align(Alignment.Center).fillMaxWidth(0.3f)
        )
        StatCard("缓存未命中", TokenFormatter.fmtTokensShort(modelData.cacheMissTokens),
            Modifier.align(Alignment.CenterEnd).fillMaxWidth(0.3f)
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatCardWide(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.End)
        )
    }
}
