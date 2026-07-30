package com.deepseek.monitor.presentation.daily

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepseek.monitor.domain.model.UsageDay
import com.deepseek.monitor.presentation.common.ErrorView
import com.deepseek.monitor.presentation.common.LoadingView
import com.deepseek.monitor.presentation.theme.EInkColors
import com.deepseek.monitor.presentation.theme.LightColors
import com.deepseek.monitor.presentation.theme.LocalEInkMode
import com.deepseek.monitor.util.TokenFormatter
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyOverviewScreen(
    onNavigateBack: () -> Unit,
    viewModel: DailyOverviewViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val today = state.todayUsage
    val isEInk = LocalEInkMode.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("当日使用情况", fontWeight = FontWeight.SemiBold)
                        Text(
                            LocalDate.now().toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 16.dp)) {
                        IconButton(
                            onClick = { viewModel.refresh() },
                            enabled = !state.isLoading,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh, "刷新",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        },
        modifier = modifier
    ) { padding ->
        when {
            state.isLoading && today == null && !isEInk -> LoadingView(message = "加载今日用量...")
            state.error != null && today == null -> ErrorView(
                message = state.error ?: "加载失败",
                onRetry = { viewModel.refresh() }
            )
            today != null -> {
                val isLandscape =
                    LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                val sw = LocalConfiguration.current.smallestScreenWidthDp
                val isTablet = sw >= 600

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp)
                ) {
                    val landscape = isLandscape || maxWidth > maxHeight

                    if (landscape) {
                        LandscapeLayout(today = today, isTablet = isTablet, isEInk = isEInk)
                    } else {
                        PortraitLayout(today = today, isTablet = isTablet, isEInk = isEInk)
                    }
                }
            }
        }
    }
}

/** 竖屏：卡片 + 费用条 + Token条 垂直堆叠 */
@Composable
private fun PortraitLayout(today: UsageDay, isTablet: Boolean, isEInk: Boolean) {
    Column(modifier = Modifier.fillMaxSize()) {
        ModelCardRow(today = today, isTablet = isTablet, isEInk = isEInk)
        Spacer(modifier = Modifier.height(16.dp))
        CostProportionBar(today = today)
        Spacer(modifier = Modifier.height(16.dp))
        TokenProportionBar(today = today)
        Spacer(modifier = Modifier.weight(1f))
    }
}

/** 横屏：左侧卡片竖排满宽 + 右侧比例条 */
@Composable
private fun LandscapeLayout(today: UsageDay, isTablet: Boolean, isEInk: Boolean) {
    val cardFraction = 0.20f

    Row(modifier = Modifier.fillMaxSize()) {
        // 左侧卡片区 — 卡片横向满填充
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(cardFraction),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            if (isTablet) {
                ModelCardWide("V4 Flash", LightColors.flash, today.flashCacheHit, today.flashCacheMiss, today.flashResponse, today.flashCost, isEInk)
                ModelCardWide("V4 Pro", LightColors.pro, today.proCacheHit, today.proCacheMiss, today.proResponse, today.proCost, isEInk)
            } else {
                ModelCardCompact("V4 Flash", LightColors.flash, today.flashCacheHit, today.flashCacheMiss, today.flashCost, isEInk,
                    Modifier.fillMaxWidth())
                ModelCardCompact("V4 Pro", LightColors.pro, today.proCacheHit, today.proCacheMiss, today.proCost, isEInk,
                    Modifier.fillMaxWidth())
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 右侧比例条
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            CostProportionBar(today = today)
            Spacer(modifier = Modifier.height(16.dp))
            TokenProportionBar(today = today)
        }
    }
}

/** 两个模型卡片横向排列 */
@Composable
private fun ModelCardRow(today: UsageDay, isTablet: Boolean, isEInk: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isTablet) {
            ModelCardWide("V4 Flash", LightColors.flash, today.flashCacheHit, today.flashCacheMiss, today.flashResponse, today.flashCost, isEInk,
                Modifier.weight(1f))
            ModelCardWide("V4 Pro", LightColors.pro, today.proCacheHit, today.proCacheMiss, today.proResponse, today.proCost, isEInk,
                Modifier.weight(1f))
        } else {
            ModelCardCompact("V4 Flash", LightColors.flash, today.flashCacheHit, today.flashCacheMiss, today.flashCost, isEInk,
                Modifier.weight(1f))
            ModelCardCompact("V4 Pro", LightColors.pro, today.proCacheHit, today.proCacheMiss, today.proCost, isEInk,
                Modifier.weight(1f))
        }
    }
}

/** 紧凑卡片（Phone 竖屏）：模型名 + 费用 + 命中率 */
@Composable
private fun ModelCardCompact(
    name: String,
    accentColor: androidx.compose.ui.graphics.Color,
    cacheHit: Long,
    cacheMiss: Long,
    cost: Double,
    isEInk: Boolean,
    modifier: Modifier = Modifier
) {
    val eAccent = if (isEInk) {
        if (accentColor == LightColors.flash) EInkColors.black else EInkColors.darkGray
    } else accentColor
    val textColor = if (isEInk) EInkColors.black else MaterialTheme.colorScheme.onSurface
    val subColor = if (isEInk) EInkColors.darkGray else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val hitRatio = TokenFormatter.cacheHitRatio(cacheHit, cacheMiss)

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(name, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, color = eAccent)
        Spacer(modifier = Modifier.height(8.dp))
        Text(TokenFormatter.fmtMoney(cost),
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
            color = textColor, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("命中率 ${TokenFormatter.fmtPercent(hitRatio)}",
            style = MaterialTheme.typography.labelMedium, color = subColor)
    }
}

/** 宽卡片（平板 / 横屏）：模型名 + 费用 + 命中/未命中/输出明细 + 命中率 */
@Composable
private fun ModelCardWide(
    name: String,
    accentColor: androidx.compose.ui.graphics.Color,
    cacheHit: Long,
    cacheMiss: Long,
    response: Long,
    cost: Double,
    isEInk: Boolean,
    modifier: Modifier = Modifier
) {
    val eAccent = if (isEInk) {
        if (accentColor == LightColors.flash) EInkColors.black else EInkColors.darkGray
    } else accentColor
    val textColor = if (isEInk) EInkColors.black else MaterialTheme.colorScheme.onSurface
    val subColor = if (isEInk) EInkColors.darkGray else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val hitRatio = TokenFormatter.cacheHitRatio(cacheHit, cacheMiss)

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(name, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, color = eAccent)
        Spacer(modifier = Modifier.height(8.dp))
        Text(TokenFormatter.fmtMoney(cost),
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
            color = textColor)
        Spacer(modifier = Modifier.height(8.dp))
        StatLine("命中", TokenFormatter.fmtTokensShort(cacheHit), textColor)
        StatLine("未命中", TokenFormatter.fmtTokensShort(cacheMiss), textColor)
        StatLine("输出", TokenFormatter.fmtTokensShort(response), textColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text("命中率 ${TokenFormatter.fmtPercent(hitRatio)}",
            style = MaterialTheme.typography.labelMedium, color = subColor)
    }
}

@Composable
private fun StatLine(label: String, value: String, textColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = 0.5f))
        Text(value, style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium, color = textColor)
    }
}
