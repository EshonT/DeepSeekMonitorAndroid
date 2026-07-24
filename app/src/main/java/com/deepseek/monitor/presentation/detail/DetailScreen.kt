package com.deepseek.monitor.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepseek.monitor.domain.model.UsageDay
import com.deepseek.monitor.domain.model.UsageModel
import com.deepseek.monitor.presentation.common.ErrorView
import com.deepseek.monitor.presentation.common.LoadingView
import com.deepseek.monitor.presentation.dashboard.DataState
import com.deepseek.monitor.util.TokenFormatter

/**
 * 模型详情页。
 * 展示单个模型（Flash/Pro）的按日 Token 消耗明细。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    model: String,
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val usageState = state.usageState
    val usageResult = state.usageResult

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val icon = if (model == "flash") "⚡" else "🧠"
                    Text("$icon V4 ${if (model == "flash") "Flash" else "Pro"}")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        },
        modifier = modifier
    ) { padding ->
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        if (modelData != null) {
                            DetailHeader(modelData)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "按日 Token 消耗",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        usage.days.take(7).forEach { day ->
                            DayUsageRow(day = day, model = model)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(modelData: UsageModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        StatCard("请求次数", TokenFormatter.fmtInt(modelData.requestCount), Modifier.weight(1f))
        Spacer(modifier = Modifier.width(12.dp))
        StatCard("Tokens", TokenFormatter.fmtTokensShort(modelData.totalTokens), Modifier.weight(1f))
        Spacer(modifier = Modifier.width(12.dp))
        StatCard("费用", TokenFormatter.fmtMoney(modelData.cost), Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DayUsageRow(day: UsageDay, model: String) {
    val tokens = if (model == "flash") day.flashTokens else day.proTokens
    val hit = if (model == "flash") day.flashCacheHit else day.proCacheHit
    val miss = if (model == "flash") day.flashCacheMiss else day.proCacheMiss
    val resp = if (model == "flash") day.flashResponse else day.proResponse

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                day.date.takeLast(5),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                TokenFormatter.fmtTokensShort(tokens) + " T",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            SubDetail("命中", hit)
            Spacer(modifier = Modifier.width(16.dp))
            SubDetail("未命中", miss)
            Spacer(modifier = Modifier.width(16.dp))
            SubDetail("输出", resp)
        }
    }
}

@Composable
private fun SubDetail(label: String, tokens: Long) {
    Text(
        "$label ${TokenFormatter.fmtTokensShort(tokens)}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )
}
