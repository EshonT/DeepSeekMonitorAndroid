package com.deepseek.monitor.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepseek.monitor.presentation.theme.LightColors

/**
 * 设置页。
 * 三大区块：API Key 管理 / 用量 Token 管理 / 自动刷新配置。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCaptureDialog by remember { mutableStateOf(false) }

    // 提取本地变量，避免委托属性智能转换问题
    val config = state.config
    val apiKeyInput = state.apiKeyInput
    val apiKeySaving = state.apiKeySaving
    val apiKeyFeedback = state.apiKeyFeedback
    val apiKeyError = state.apiKeyError
    val usageTokenInput = state.usageTokenInput
    val usageTokenSaving = state.usageTokenSaving
    val usageTokenFeedback = state.usageTokenFeedback
    val usageTokenError = state.usageTokenError

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.SemiBold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // ── API Key 区块 ──
            ApiKeySection(
                configured = config.apiKeyConfigured,
                preview = config.apiKeyPreview,
                input = apiKeyInput,
                saving = apiKeySaving,
                feedback = apiKeyFeedback,
                isError = apiKeyError,
                onInputChanged = viewModel::onApiKeyInputChanged,
                onSave = viewModel::saveApiKey,
                onClear = viewModel::clearApiKey
            )

            // ── 用量 Token 区块 ──
            SectionHeader(icon = "📊", title = "用量同步 Token")

            Spacer(modifier = Modifier.height(8.dp))

            // 网页登录按钮
            Button(
                onClick = { showCaptureDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightColors.primary)
            ) {
                Text("网页登录自动同步")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (config.usageTokenConfigured) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = LightColors.success,
                        modifier = Modifier.height(18.dp)
                    )
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Text(
                        "已配置",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = viewModel::clearUsageToken,
                    modifier = Modifier,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("清除 Token")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = usageTokenInput,
                onValueChange = viewModel::onUsageTokenInputChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("粘贴用量 Token") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            if (usageTokenInput.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = viewModel::saveUsageToken,
                    enabled = !usageTokenSaving,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LightColors.primary)
                ) {
                    Text(if (usageTokenSaving) "验证中..." else "保存 Token")
                }
            }

            val feedback = usageTokenFeedback
            if (feedback != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = feedback,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (usageTokenError) LightColors.error else LightColors.success,
                    modifier = Modifier
                )
            }

            // ── 自动刷新区块 ──
            SectionHeader(icon = "🔄", title = "自动刷新")

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "启用自动刷新",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = config.autoRefreshEnabled,
                    onCheckedChange = viewModel::setAutoRefresh
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "刷新间隔",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
            )

            Spacer(modifier = Modifier.height(6.dp))

            RefreshIntervalSelector(
                current = config.refreshIntervalSeconds,
                onSelect = viewModel::setRefreshInterval,
                modifier = Modifier
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "DeepSeek Monitor Android v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                modifier = Modifier
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Token 捕获对话框
    if (showCaptureDialog) {
        // 打开对话框时清除之前的错误反馈
        LaunchedEffect(Unit) {
            viewModel.onUsageTokenInputChanged("")
        }
        TokenCaptureDialog(
            onTokenCaptured = { token ->
                // 自动填入输入框
                viewModel.onUsageTokenInputChanged(token)
                // 用户手动点「保存 Token」触发验证
            },
            onDismiss = { showCaptureDialog = false }
        )
    }
}

/**
 * 刷新间隔选择器（单行 RadioButton）。
 */
@Composable
private fun RefreshIntervalSelector(
    current: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        60 to "1分钟",
        300 to "5分钟",
        1800 to "30分钟",
        3600 to "1小时"
    )

    Row(
        modifier = modifier.selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { (seconds, label) ->
            Row(
                modifier = Modifier
                    .selectable(
                        selected = current == seconds,
                        onClick = { onSelect(seconds) },
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = current == seconds, onClick = null)
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}
